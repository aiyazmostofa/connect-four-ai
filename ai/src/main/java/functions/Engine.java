package functions;

import java.util.Stack;

public class Engine {
    private static final int ROWS = 6, COLUMNS = 7;
    private static final int DEPTH = 7;
    private int[] blankSpots;
    private Stack<Integer> indicesPlayed;
    private int moves;

    public int compute(char[][] matrix) {
        blankSpots = new int[COLUMNS];
        indicesPlayed = new Stack<>();
        moves = 0;
        for (int i = 0; i < COLUMNS; i++) {
            for (int j = ROWS - 1; j >= -1; j--) {
                if (j != -1 && matrix[j][i] != '-') {
                    moves++;
                    continue;
                }
                blankSpots[i] = j;
                break;
            }
        }

        if (blankSpots[COLUMNS / 2] >= ROWS - 2) {
            return COLUMNS / 2;
        }

        int index = 0;
        double max = Double.NEGATIVE_INFINITY;
        int minDepth = DEPTH;
        for (int i = 0; i < COLUMNS; i++) {
            if (blankSpots[i] == -1) {
                if (index == i) index++;
                continue;
            }
            add(matrix, i, true);
            Object[] temp = minimax(matrix, DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
            remove(matrix, i);
            if ((double) temp[0] > max || (double) temp[0] == max && (int) temp[1] < minDepth) {
                index = i;
                max = (double) temp[0];
                minDepth = (int) temp[1];
            }
        }

        return index;
    }

    private Object[] minimax(char[][] matrix, int limit, double alpha, double beta, boolean maximizing) {
        if (hasWinner(matrix)) {
            double score = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            return new Object[]{score, DEPTH - limit};
        }
        if (moves == COLUMNS * ROWS) return new Object[]{0.0, DEPTH - limit};
        if (limit == 0) {
            return new Object[]{evaluate(matrix), DEPTH};
        }

        double result;
        int maxMinDepth = maximizing ? DEPTH : 0;
        if (maximizing) {
            result = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < COLUMNS; i++) {
                if (blankSpots[i] == -1) continue;
                add(matrix, i, true);
                Object[] temp = minimax(matrix, limit - 1, alpha, beta, false);
                remove(matrix, i);
                if ((double) temp[0] > result || (double) temp[0] == result && (int) temp[1] < maxMinDepth) {
                    result = (double) temp[0];
                    maxMinDepth = (int) temp[1];
                }

                alpha = Math.max(alpha, (double) temp[0]);
                if (beta <= alpha) break;
            }
        } else {
            result = Double.POSITIVE_INFINITY;
            for (int i = 0; i < COLUMNS; i++) {
                if (blankSpots[i] == -1) continue;
                add(matrix, i, false);
                Object[] temp = minimax(matrix, limit - 1, alpha, beta, true);
                remove(matrix, i);
                if ((double) temp[0] < result || (double) temp[0] == result && (int) temp[1] > maxMinDepth) {
                    result = (double) temp[0];
                    maxMinDepth = (int) temp[1];
                }

                beta = Math.min(beta, (double) temp[0]);
                if (beta <= alpha) break;
            }
        }
        return new Object[]{result, maxMinDepth};
    }

    private void add(char[][] matrix, int column, boolean self) {
        while (blankSpots[column] == -1) column++;
        matrix[blankSpots[column]][column] = self ? 'X' : 'O';
        moves++;
        blankSpots[column]--;
        indicesPlayed.push(column);
    }

    private void remove(char[][] matrix, int column) {
        blankSpots[column]++;
        moves--;
        matrix[blankSpots[column]][column] = '-';
        indicesPlayed.pop();
    }

    private boolean hasWinner(char[][] matrix) {
        final int c = indicesPlayed.peek();
        final int r = blankSpots[c] + 1;

        int x;
        int o;

        x = o = 0;
        for (int i = -3; i <= 3; i++) {
            if (c + i < 0 || c + i >= COLUMNS) continue;
            if (matrix[r][c + i] == 'X') {
                x++;
                o = 0;
            }
            if (matrix[r][c + i] == 'O') {
                x = 0;
                o++;
            }
            if (matrix[r][c + i] == '-') {
                x = o = 0;
            }
            if (x == 4 || o == 4) return true;
        }

        x = o = 0;
        for (int i = -3; i <= 3; i++) {
            if (r + i < 0 || r + i >= ROWS) continue;
            if (matrix[r + i][c] == 'X') {
                x++;
                o = 0;
            }
            if (matrix[r + i][c] == 'O') {
                x = 0;
                o++;
            }
            if (matrix[r + i][c] == '-') {
                x = o = 0;
            }
            if (x == 4 || o == 4) return true;
        }

        x = o = 0;
        for (int i = -3; i <= 3; i++) {
            if (r + i < 0 || r + i >= ROWS || c + i < 0 || c + i >= COLUMNS) continue;
            if (matrix[r + i][c + i] == 'X') {
                x++;
                o = 0;
            }
            if (matrix[r + i][c + i] == 'O') {
                x = 0;
                o++;
            }
            if (matrix[r + i][c + i] == '-') {
                x = o = 0;
            }
            if (x == 4 || o == 4) return true;
        }

        x = o = 0;
        for (int i = -3; i <= 3; i++) {
            if (r + i < 0 || r + i >= ROWS || c - i < 0 || c - i >= COLUMNS) continue;
            if (matrix[r + i][c - i] == 'X') {
                x++;
                o = 0;
            }
            if (matrix[r + i][c - i] == 'O') {
                x = 0;
                o++;
            }
            if (matrix[r + i][c - i] == '-') {
                x = o = 0;
            }
            if (x == 4 || o == 4) return true;
        }
        return false;
    }

    private double evaluate(char[][] matrix) {
        int oneAwayForSelf = 0;
        int twoAwayForSelf = 0;
        int oneAwayForOther = 0;

        for (int r = 0; r < ROWS; r++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int c = 0; c < COLUMNS; c++) {
                if (matrix[r][c] == '-') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[r][c - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == 'X') {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                        }
                        if (matrix[r][c - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[r][c] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled >= 4)
                            return current == 'X' ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == 'X') {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                            }
                            if (matrix[r][c - (blanks + filled - 1)] == '-') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[r][c];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        for (int c = 0; c < COLUMNS; c++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int r = 0; r < ROWS; r++) {
                if (matrix[r][c] == '-') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[r - (blanks + filled - 1)][c] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == 'X') {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                        }
                        if (matrix[r - (blanks + filled - 1)][c] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[r][c] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == 'X' ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == 'X') {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                            }
                            if (matrix[r - (blanks + filled - 1)][c] == '-') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[r][c];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        for (int r = 0; r < ROWS - 1; r++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int c = 0; c < COLUMNS && r + c < ROWS; c++) {
                int rc = r + c;
                if (matrix[rc][c] == '-') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[rc - (blanks + filled - 1)][c - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == 'X') {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                        }
                        if (matrix[rc - (blanks + filled - 1)][c - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[rc][c] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == 'X' ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == 'X') {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                            }
                            if (matrix[rc - (blanks + filled - 1)][c - (blanks + filled - 1)] == '-') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[rc][c];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        for (int c = 1; c < COLUMNS; c++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int r = 0; r < ROWS && r + c < COLUMNS; r++) {
                int rc = r + c;
                if (matrix[r][rc] == '-') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[r - (blanks + filled - 1)][rc - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == 'X') {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                        }
                        if (matrix[r - (blanks + filled - 1)][rc - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[r][rc] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == 'X' ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == 'X') {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                            }
                            if (matrix[r - (blanks + filled - 1)][rc - (blanks + filled - 1)] == '-') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[r][rc];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        for (int r = ROWS - 1; r >= 0; r--) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int c = 0; c < COLUMNS && r - c >= 0; c++) {
                int rc = r - c;
                if (matrix[rc][c] == '-') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[rc + (blanks + filled - 1)][c - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == 'X') {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                        }
                        if (matrix[rc + (blanks + filled - 1)][c - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[rc][c] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == 'X' ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == 'X') {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                            }
                            if (matrix[rc + (blanks + filled - 1)][c - (blanks + filled - 1)] == '-') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[rc][c];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        for (int c = 1; c < COLUMNS; c++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int r = ROWS - 1; r >= 0 && c + (ROWS - 1 - r) < COLUMNS; r--) {
                int rc = (ROWS - 1 - r) + c;
                if (matrix[r][rc] == '-') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[r + (blanks + filled - 1)][rc - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == 'X') {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                        }
                        if (matrix[r + (blanks + filled - 1)][rc - (blanks + filled - 1)] == '-') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[r][rc] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == 'X' ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == 'X') {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                            }
                            if (matrix[r + (blanks + filled - 1)][rc - (blanks + filled - 1)] == '-') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[r][rc];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        return 125.0 * oneAwayForSelf + 64.0 * twoAwayForSelf - 64.0 * oneAwayForOther;
    }
}