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
 */
public final class TcpForwarder implements AutoCloseable {

    private final ServerSocket server;
    private final List<Socket> sockets = new CopyOnWriteArrayList<>();

    private TcpForwarder(ServerSocket server, int targetPort) {
        this.server = server;
        Thread acceptor = new Thread(() -> accept(targetPort), "tcp-forwarder-" + server.getLocalPort());
        acceptor.setDaemon(true);
        acceptor.start();
    }

    /** Escuta em {@code localhost:listenPort} e repassa cada conexão para {@code localhost:targetPort}. */
    public static TcpForwarder start(int listenPort, int targetPort) {
        try {
            ServerSocket server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), listenPort));
            return new TcpForwarder(server, targetPort);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void accept(int targetPort) {
        while (!server.isClosed()) {
            try {
                Socket client = server.accept();
                Socket target = new Socket(InetAddress.getLoopbackAddress(), targetPort);
                sockets.add(client);
                sockets.add(target);
                pump(client, target);
                pump(target, client);
            } catch (IOException e) {
                // Servidor fechado, ou o destino recusou: a próxima conexão tenta de novo.
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
