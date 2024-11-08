import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConverter {
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Select base currency
        System.out.print("Enter the base currency (e.g., USD, EUR): ");
        String baseCurrency = scanner.nextLine().toUpperCase();

        // Step 2: Fetch currency rates
        Map<String, Double> rates = fetchCurrencyRates(baseCurrency);
        if (rates == null) {
            System.out.println("Could not fetch exchange rates.");
            return;
        }

        // Step 3: Display available currencies
        System.out.println("Available currencies:");
        for (String currency : rates.keySet()) {
            System.out.println(currency);
        }

        // Step 4: Select target currency
        System.out.print("Enter the target currency: ");
        String targetCurrency = scanner.nextLine().toUpperCase();

        // Check if target currency is valid
        if (!rates.containsKey(targetCurrency)) {
            System.out.println("Invalid target currency.");
            return;
        }

        // Step 5: Input amount to convert
        System.out.print("Enter amount in " + baseCurrency + ": ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        // Step 6: Perform conversion
        double convertedAmount = convertCurrency(amount, baseCurrency, targetCurrency, rates);

        // Step 7: Display result
        System.out.printf("%.2f %s = %.2f %s%n", amount, baseCurrency, convertedAmount, targetCurrency);
    }

    private static Map<String, Double> fetchCurrencyRates(String baseCurrency) {
        try {
            URL url = new URL(API_URL + baseCurrency);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                return null; // Handle error response
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON response
            String jsonResponse = response.toString();
            return parseCurrencyRates(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String, Double> parseCurrencyRates(String jsonResponse) {
        Map<String, Double> rates = new HashMap<>();
        jsonResponse = jsonResponse.substring(jsonResponse.indexOf('{') + 1);
        String[] entries = jsonResponse.split(",");

        for (String entry : entries) {
            String[] keyValue = entry.split(":");
            String currency = keyValue[0].replaceAll("\"", "").trim();
            double rate = Double.parseDouble(keyValue[1]);
            rates.put(currency, rate);
        }

        return rates;
    }

    private static double convertCurrency(double amount, String baseCurrency, String targetCurrency, Map<String, Double> rates) {
        double baseRate = rates.get(baseCurrency);
        double targetRate = rates.get(targetCurrency);
        return amount * (targetRate / baseRate);
    }
}