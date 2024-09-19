import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;

public class Jugador {
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static JButton shootButton;
    private static JButton reloadButton;
    private static JButton coverButton;
    private static JTextArea statusArea;
    private static JPanel imagePanel;
    private static Timer animationTimer;
    private static Timer imageDisplayTimer;
    private static int currentFrame = 0;
    private static boolean gameEnded = false;

    // Array de imágenes para la animación
    private static final BufferedImage[] ANIMATION_FRAMES = new BufferedImage[4];
    private static BufferedImage SHOOT_IMAGE;
    private static BufferedImage RELOAD_IMAGE;
    private static BufferedImage COVER_IMAGE;
    private static final int IMAGE_DISPLAY_DURATION = 3000; // Duración en milisegundos para mostrar imagen seleccionada

    public static void main(String[] args) {
        loadImages(); // Cargar imágenes desde los archivos

        JFrame frame = new JFrame("Pistolero");
        frame.setSize(1000, 600); // Tamaño aumentado para mejor disposición
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setBackground(new Color(40, 44, 52));

        // Crear panel para botones con un diseño moderno
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(new Color(30, 30, 30));

        shootButton = createStyledButton("Disparar", "path/to/shoot_icon.png");
        reloadButton = createStyledButton("Recargar", "path/to/reload_icon.png");
        coverButton = createStyledButton("Cubrirse", "path/to/cover_icon.png");

        buttonPanel.add(shootButton);
        buttonPanel.add(reloadButton);
        buttonPanel.add(coverButton);

        // Crear área de estado con diseño actualizado
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Arial", Font.PLAIN, 14));
        statusArea.setBackground(new Color(30, 30, 30));
        statusArea.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 2),
            "Estado del Juego",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 16),
            Color.DARK_GRAY
        ));

        // Crear panel de imagen para animación
        imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(300, 300)); // Tamaño ajustado según sea necesario
        imagePanel.setBackground(new Color(20, 20, 20));
        JLabel imageLabel = new JLabel();
        imagePanel.add(imageLabel);

        // Agregar componentes al marco
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(imagePanel, BorderLayout.WEST);

        // Acciones de los botones
        shootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameEnded) {
                    handleButtonClick(SHOOT_IMAGE);
                    sendChoice("Disparar");
                }
            }
        });

        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameEnded) {
                    handleButtonClick(RELOAD_IMAGE);
                    sendChoice("Recargar");
                }
            }
        });

        coverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameEnded) {
                    handleButtonClick(COVER_IMAGE);
                    sendChoice("Cubrirse");
                }
            }
        });

        // Iniciar animación
        startAnimation(imagePanel);

        frame.setVisible(true);

        // Conectar al servidor
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                statusArea.append(serverMessage + "\n");
                if (serverMessage.contains("¡Ganaste!") || serverMessage.contains("Perdiste")) {
                    disableButtons();
                    stopAnimation(); // Detener la animación si el juego termina
                    gameEnded = true; // Marcar que el juego ha terminado
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleButtonClick(BufferedImage selectedImage) {
        // Detener la animación y mostrar la imagen seleccionada
        stopAnimation();
        showImage(selectedImage);

        // Deshabilitar los botones
        disableButtons();

        // Programar la reactivación de los botones después de un lapso
        if (imageDisplayTimer != null && imageDisplayTimer.isRunning()) {
            imageDisplayTimer.stop(); // Detener el temporizador si ya está corriendo
        }
        imageDisplayTimer = new Timer(IMAGE_DISPLAY_DURATION, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Solo reiniciar la animación si el juego no ha terminado
                if (!gameEnded) {
                    restartAnimation();
                }
                enableButtons();
                imageDisplayTimer.stop(); // Detener el temporizador después de ejecutar
            }
        });
        imageDisplayTimer.setRepeats(false); // Asegúrate de que el temporizador no repita
        imageDisplayTimer.start();
    }

    private static void sendChoice(String choice) {
        if (out != null) {
            out.println(choice);
        }
    }

    private static void disableButtons() {
        shootButton.setEnabled(false);
        reloadButton.setEnabled(false);
        coverButton.setEnabled(false);
    }

    private static void enableButtons() {
        shootButton.setEnabled(true);
        reloadButton.setEnabled(true);
        coverButton.setEnabled(true);
    }

    private static void startAnimation(JPanel panel) {
        JLabel imageLabel = (JLabel) panel.getComponent(0);

        animationTimer = new Timer(750, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameEnded) {
                    imageLabel.setIcon(new ImageIcon(ANIMATION_FRAMES[currentFrame]));
                    currentFrame = (currentFrame + 1) % ANIMATION_FRAMES.length;
                }
            }
        });

        animationTimer.start();
    }

    private static void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    private static void restartAnimation() {
        currentFrame = 0;
        startAnimation(imagePanel);
    }

    private static void showImage(BufferedImage image) {
        JLabel imageLabel = (JLabel) imagePanel.getComponent(0);
        imageLabel.setIcon(new ImageIcon(image));
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    // Cargar imágenes desde archivos en lugar de crearlas dinámicamente
    private static void loadImages() {
        try {
            for (int i = 0; i < ANIMATION_FRAMES.length; i++) {
                ANIMATION_FRAMES[i] = ImageIO.read(new File("src/images/Frame" + (i + 1) + ".png"));
            }
            SHOOT_IMAGE = ImageIO.read(new File("src/images/Shoot_image.png"));
            RELOAD_IMAGE = ImageIO.read(new File("src/images/Reload_image.png"));
            COVER_IMAGE = ImageIO.read(new File("src/images/Cover_image.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setIcon(new ImageIcon(iconPath));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(60, 63, 65));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        button.setPreferredSize(new Dimension(150, 50));

        return button;
    }
}
