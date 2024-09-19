import java.io.*;
import java.net.*;

public class Server {
    private static final int INITIAL_BULLETS = 1;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345);
             Socket player1 = serverSocket.accept();
             Socket player2 = serverSocket.accept();
             BufferedReader in1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
             PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
             BufferedReader in2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
             PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true)) {

            int player1Bullets = INITIAL_BULLETS;
            int player2Bullets = INITIAL_BULLETS;
            boolean player1Covered = false;
            boolean player2Covered = false;

            boolean gameRunning = true;
            while (gameRunning) {
                String choice1 = in1.readLine();
                String choice2 = in2.readLine();

                // Check for valid inputs
                if (choice1 == null || choice2 == null) break;

                // Update cover status
                player1Covered = "Cubrirse".equals(choice1);
                player2Covered = "Cubrirse".equals(choice2);

                // Handle actions and update bullets
                if ("Disparar".equals(choice1)) {
                    if (player1Bullets > 0) {
                        player1Bullets--;
                    } else {
                        out1.println("Intentaste disparar pero no tienes balas.");
                        out2.println("El jugador 1 intentó disparar pero no tenía balas.");
                        continue;
                    }
                }

                if ("Disparar".equals(choice2)) {
                    if (player2Bullets > 0) {
                        player2Bullets--;
                    } else {
                        out2.println("Intentaste disparar pero no tienes balas.");
                        out1.println("El jugador 2 intentó disparar pero no tenía balas.");
                        continue;
                    }
                }

                // Determine the outcome based on player actions
                if ("Disparar".equals(choice1) && !player2Covered) {
                    out1.println("¡Ganaste! El jugador 2 no estaba cubierto.");
                    out2.println("Perdiste. El jugador 1 disparó y tú no estabas cubierto.");
                    gameRunning = false;
                } else if ("Disparar".equals(choice2) && !player1Covered) {
                    out1.println("Perdiste. El jugador 2 disparó y tú no estabas cubierto.");
                    out2.println("¡Ganaste! El jugador 1 no estaba cubierto.");
                    gameRunning = false;
                } else {
                    // Handle reload messages
                    if ("Recargar".equals(choice1)) {
                        player1Bullets++;
                        out1.println("Elegiste recargar y ahora tienes " + player1Bullets + " balas.");
                        out2.println("El jugador 1 recargó. Balas restantes: " + player1Bullets);
                    }
                    if ("Recargar".equals(choice2)) {
                        player2Bullets++;
                        out1.println("El jugador 2 recargó. Balas restantes: " + player2Bullets);
                        out2.println("Elegiste recargar y ahora tienes " + player2Bullets + " balas.");
                    }
                    if (!"Recargar".equals(choice1) && !"Recargar".equals(choice2)) {
                        out1.println("Ronda empatada");
                        out2.println("Ronda empatada");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
