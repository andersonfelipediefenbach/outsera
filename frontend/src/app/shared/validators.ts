import { AbstractControl, ValidationErrors } from '@angular/forms';

/** Ano: inteiro não negativo, só dígitos, até 9 (o mesmo limite da API). */
const YEAR = /^\d{1,9}$/;

/** Mensagem dos campos de ano (contracts/frontend-ui.md). */
export const YEAR_MESSAGE = 'Informe um ano válido (só números).';

/** O ano do texto, ou `undefined` quando o texto não é um ano válido (spec 006, FR-008, FR-011 e FR-013). */
export function parseYear(text: string | null | undefined): number | undefined {
  const value = (text ?? '').trim();
  return YEAR.test(value) ? Number(value) : undefined;
}

/** Validador de formulário para os campos de ano. */
export function yearValidator(control: AbstractControl<string | null>): ValidationErrors | null {
  return parseYear(control.value) === undefined ? { ano: true } : null;
}
