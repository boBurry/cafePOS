package Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramService {

    private static final String BOT_TOKEN = "8492743804:AAEoaaaTiAgj_X_0Aj1tCX2MRtLDBZ7tv-Y"; 
    private static final String CHAT_ID = "807381574";     

    public static void sendOrderNotification(int orderId, double total, String paymentType) {
        // Run in a separate thread to avoid freezing the POS UI
        new Thread(() -> {
            try {
                String message = "🔔 *New Order Received!* 🔔\n"
                        + "Order ID: #" + orderId + "\n"
                        + "Total: $" + String.format("%.2f", total) + "\n"
                        + "Payment: " + paymentType + "\n"
                        + "Status: Paid ✅";

                String encodedMessage = URLEncoder.encode(message, "UTF-8");
                
                String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + CHAT_ID + "&text=" + encodedMessage + "&parse_mode=Markdown";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Trigger the request
                int responseCode = conn.getResponseCode();
                
                // Optional: Read response for debugging
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("Telegram Notification Sent: " + responseCode);

            } catch (Exception e) {
                System.err.println("Failed to send Telegram notification: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}