package org.dubini.gestion.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NifCifValidator implements ConstraintValidator<ValidNifCif, String> {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        String clean = value.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (clean.length() != 9) {
            return false;
        }

        char first = clean.charAt(0);
        char last = clean.charAt(8);

        // DNI: 8 dígitos + 1 letra
        if (Character.isDigit(first)) {
            return validateDni(clean, last);
        }

        // NIE: X/Y/Z + 7 dígitos + 1 letra
        if (first == 'X' || first == 'Y' || first == 'Z') {
            return validateNie(first, clean, last);
        }

        // CIF / NIF Corporativo: Letra + 7 dígitos + dígito/letra de control
        if ("ABCDEFGHJNPQRSTUVW".indexOf(first) != -1) {
            return validateCifFormat(clean);
        }

        return false;
    }

    private boolean validateDni(String clean, char last) {
        if (!clean.substring(0, 8).matches("\\d{8}") || !Character.isLetter(last)) {
            return false;
        }
        return validateDniNieLetter(clean.substring(0, 8), last);
    }

    private boolean validateNie(char first, String clean, char last) {
        if (!clean.substring(1, 8).matches("\\d{7}") || !Character.isLetter(last)) {
            return false;
        }
        String prefix = "";
        if (first == 'X') {
            prefix = "0";
        } else if (first == 'Y') {
            prefix = "1";
        } else if (first == 'Z') {
            prefix = "2";
        }
        return validateDniNieLetter(prefix + clean.substring(1, 8), last);
    }

    private boolean validateCifFormat(String clean) {
        if (!clean.substring(1, 8).matches("\\d{7}")) {
            return false;
        }
        return validateCif(clean);
    }

    private boolean validateDniNieLetter(String numbers, char letter) {
        try {
            int val = Integer.parseInt(numbers);
            int rem = val % 23;
            char expected = DNI_LETTERS.charAt(rem);
            return expected == letter;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateCif(String cif) {
        String digits = cif.substring(1, 8);
        char control = cif.charAt(8);

        int evenSum = 0;
        int oddSum = 0;

        for (int i = 0; i < 7; i++) {
            int d = Character.getNumericValue(digits.charAt(i));
            if (i % 2 == 1) { // Índice base 0 impar: 2º, 4º y 6º dígitos
                evenSum += d;
            } else { // Índice base 0 par: 1º, 3º, 5º y 7º dígitos
                int doubled = d * 2;
                oddSum += (doubled > 9) ? (doubled - 9) : doubled;
            }
        }

        int totalSum = evenSum + oddSum;
        int rem = totalSum % 10;
        int d = (rem == 0) ? 0 : (10 - rem);

        char expectedDigit = (char) ('0' + d);
        char expectedLetter = "JABCDEFGHI".charAt(d);

        return control == expectedDigit || control == expectedLetter;
    }
}
