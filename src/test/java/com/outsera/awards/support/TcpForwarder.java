package com.outsera.awards.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Repassador TCP de uma porta local para outra, para os testes em que a dependência precisa aparecer
 * numa porta que a aplicação já usa (spec 003, {@code KafkaBrokerLateStartIT}).
 *
 * <p>Não é um dublê: só copia bytes entre a aplicação e o serviço real, sem ler nem alterar o protocolo.
 * Existe porque o {@code EmbeddedKafkaKraftBroker} do Spring Kafka 4.1.2 ignora o {@code kafkaPorts} e sempre
 * sobe numa porta aleatória.
 *
 * <p>A porta fica reservada desde {@link #reserve()}: enquanto não há destino, cada conexão é fechada logo
 * depois de aceita, como um serviço fora do ar. Sem a reserva, a porta ficaria livre entre a escolha e o
 * uso, e outro broker embutido da mesma JVM poderia ocupá-la (falha vista no pipeline do PR do Marco 3).
 */
public final class TcpForwarder implements AutoCloseable {

    private final ServerSocket server;
    private final List<Socket> sockets = new CopyOnWriteArrayList<>();
    private volatile int targetPort;

    private TcpForwarder(ServerSocket server) {
        this.server = server;
        Thread acceptor = new Thread(this::accept, "tcp-forwarder-" + server.getLocalPort());
        acceptor.setDaemon(true);
        acceptor.start();
    }

    /** Reserva uma porta livre em {@code localhost}, ainda sem destino. */
    public static TcpForwarder reserve() {
        try {
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            return new TcpForwarder(server);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A porta reservada. */
    public int port() {
        return server.getLocalPort();
    }

    /** Passa a repassar as conexões novas para {@code localhost:targetPort}. */
    public void forwardTo(int targetPort) {
        this.targetPort = targetPort;
    }

    private void accept() {
        while (!server.isClosed()) {
            Socket client;
            try {
                client = server.accept();
            } catch (IOException e) {
                continue; // Servidor fechado: o laço termina.
            }
            int target = targetPort;
            if (target == 0) {
                closeQuietly(client); // Ainda sem destino: o serviço está fora do ar.
                continue;
            }
            try {
                Socket upstream = new Socket(InetAddress.getLoopbackAddress(), target);
                sockets.add(client);
                sockets.add(upstream);
                pump(client, upstream);
                pump(upstream, client);
            } catch (IOException e) {
                closeQuietly(client); // O destino recusou: a próxima conexão tenta de novo.
            }
        }
    }

    private static void pump(Socket from, Socket to) {
        Thread thread = new Thread(() -> {
            try (InputStream in = from.getInputStream(); OutputStream out = to.getOutputStream()) {
                in.transferTo(out);
            } catch (IOException e) {
                // Conexão encerrada por um dos lados.
            } finally {
                closeQuietly(from);
                closeQuietly(to);
            }
        }, "tcp-forwarder-pump");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void close() {
        closeQuietly(server);
        sockets.forEach(TcpForwarder::closeQuietly);
    }

    private static void closeQuietly(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            // Já fechado.
        }
    }
}
