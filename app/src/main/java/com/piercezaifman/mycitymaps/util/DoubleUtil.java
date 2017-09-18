package com.piercezaifman.mycitymaps.util;

/*
 Copyright (c) 2015, Laurent Bourges. All rights reserved.
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class DoubleUtil {


    private final static boolean USE_POW_TABLE = true;

    // Precompute Math.pow(10, n) as table:
    private final static int POW_RANGE = (USE_POW_TABLE) ? 256 : 0;
    private final static double[] POS_EXPS = new double[POW_RANGE];
    private final static double[] NEG_EXPS = new double[POW_RANGE];

    static {
        for (int i = 0; i < POW_RANGE; i++) {
            POS_EXPS[i] = Math.pow(10., i);
            NEG_EXPS[i] = Math.pow(10., -i);
        }
    }

    public static double getDouble(final CharSequence csq) throws NumberFormatException {
        return getDouble(csq, 0, csq.length());
    }

    public static double getDouble(final CharSequence csq,
                                   final int offset, final int end) throws NumberFormatException {

        int off = offset;
        int len = end - offset;

        if (len == 0) {
            return Double.NaN;
        }

        char ch;
        boolean numSign = true;

        ch = csq.charAt(off);
        if (ch == '+') {
            off++;
            len--;
        } else if (ch == '-') {
            numSign = false;
            off++;
            len--;
        }

        double number;

        // Look for the special csqings NaN, Inf,
        if (len >= 3
                && ((ch = csq.charAt(off)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 1)) == 'a' || ch == 'A')
                && ((ch = csq.charAt(off + 2)) == 'n' || ch == 'N')) {

            number = Double.NaN;

            // Look for the longer csqing first then try the shorter.
        } else if (len >= 8
                && ((ch = csq.charAt(off)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 1)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 2)) == 'f' || ch == 'F')
                && ((ch = csq.charAt(off + 3)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 4)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 5)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 6)) == 't' || ch == 'T')
                && ((ch = csq.charAt(off + 7)) == 'y' || ch == 'Y')) {

            number = Double.POSITIVE_INFINITY;

        } else if (len >= 3
                && ((ch = csq.charAt(off)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 1)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 2)) == 'f' || ch == 'F')) {

            number = Double.POSITIVE_INFINITY;

        } else {

            boolean error = true;

            int startOffset = off;
            double dval;

            // TODO: check too many digits (overflow)
            for (dval = 0d; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9');) {
                dval *= 10d;
                dval += ch - '0';
                off++;
                len--;
            }
            int numberLength = off - startOffset;

            number = dval;

            if (numberLength > 0) {
                error = false;
            }

            // Check for fractional values after decimal
            if ((len > 0) && (csq.charAt(off) == '.')) {

                off++;
                len--;

                startOffset = off;

                // TODO: check too many digits (overflow)
                for (dval = 0d; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9');) {
                    dval *= 10d;
                    dval += ch - '0';
                    off++;
                    len--;
                }
                numberLength = off - startOffset;

                if (numberLength > 0) {
                    // TODO: try factorizing pow10 with exponent below: only 1 long + operation
                    number += getPow10(-numberLength) * dval;
                    error = false;
                }
            }

            if (error) {
                throw new NumberFormatException("Invalid Double : " + csq);
            }

            // Look for an exponent
            if (len > 0) {
                // note: ignore any non-digit character at end:

                if ((ch = csq.charAt(off)) == 'e' || ch == 'E') {

                    off++;
                    len--;

                    if (len > 0) {
                        boolean expSign = true;

                        ch = csq.charAt(off);
                        if (ch == '+') {
                            off++;
                            len--;
                        } else if (ch == '-') {
                            expSign = false;
                            off++;
                            len--;
                        }

                        int exponent = 0;

                        // note: ignore any non-digit character at end:
                        for (exponent = 0; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9');) {
                            exponent *= 10;
                            exponent += ch - '0';
                            off++;
                            len--;
                        }

                        // TODO: check exponent < 1024 (overflow)
                        if (!expSign) {
                            exponent = -exponent;
                        }

                        // For very small numbers we try to miminize
                        // effects of denormalization.
                        if (exponent > -300) {
                            // TODO: cache Math.pow ?? see web page
                            number *= getPow10(exponent);
                        } else {
                            number = 1.0E-300 * (number * getPow10(exponent + 300));
                        }
                    }
                }
            }
            // check other characters:
            if (len > 0) {
                throw new NumberFormatException("Invalid Double : " + csq);
            }
        }

        return (numSign) ? number : -number;
    }


    private final static double getPow10(final int exp) {
        if (USE_POW_TABLE) {
            if (exp > -POW_RANGE) {
                if (exp <= 0) {
                    return NEG_EXPS[-exp];
                } else if (exp < POW_RANGE) {
                    return POS_EXPS[exp];
                }
            }
        }
        return Math.pow(10., exp);
    }
}
