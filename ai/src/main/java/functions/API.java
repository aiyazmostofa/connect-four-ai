package functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;

public class API implements HttpFunction {
    @Override
    public void service(HttpRequest request, HttpResponse response)
            throws IOException {
        BufferedWriter writer = response.getWriter();
        String board = request.getQuery().get();
        board = board.substring(board.indexOf("=") + 1);
        Engine ai = new Engine();
        char[][] matrix = new char[6][7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                matrix[i][j] = board.charAt(i * 7 + j);
            }
        }
        int index = ai.compute(matrix);
        writer.write(index + "");
    }
}