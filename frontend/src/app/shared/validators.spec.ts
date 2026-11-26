import { FormControl } from '@angular/forms';
import { parseYear, yearValidator } from './validators';

describe('validação do ano', () => {
  it.each([
    ['1990', 1990],
    ['0', 0],
    [' 2015 ', 2015],
  ])('aceita %j', (text, year) => {
    expect(parseYear(text)).toBe(year);
    expect(yearValidator(new FormControl(text))).toBeNull();
  });

  it.each(['', '   ', 'abc', '-1', '19.5', '1e3', '19 90', '1234567890', null, undefined])('recusa %j', (text) => {
    expect(parseYear(text)).toBeUndefined();
    expect(yearValidator(new FormControl(text ?? null))).toEqual({ ano: true });
  });
});
