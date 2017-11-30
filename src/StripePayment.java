
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Account;
import com.stripe.model.AccountCollection;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import com.stripe.model.Token;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dmitry
 */
public class StripePayment {

    private static final String[] API_KEYS = new String[]{"c2tfbGl2ZV8wQ0NqQWk2dFVPZHkwVkRDMXBzR0dtbUU=", "c2tfbGl2ZV92bXRQeU41VTg2dGtGT0Z1cGd5R085STE=",
        "c2tfbGl2ZV8zOEQyVDVzUHVEdWQ1TEFKZ3B5bkFBUno=", "c2tfbGl2ZV90YklEOEIxU1h1WERNWkxySWU1OGpWeG4="};

    public static void main(String[] args) throws Exception {
        Stripe.apiKey = "sk_live_2UaRbwJpkIqHuIFHWpR7Mt8x";

        Map<String, Object> accountParams = new HashMap<String, Object>();
        accountParams.put("limit", 3);

        Account resp = Account.retrieve();

            System.out.println(resp.toString());

        
    }

    private static synchronized Customer getCustomer(Token token, String order, String email) throws Exception {
        Card card = null;
        Map<String, Object> customerParams = new HashMap<String, Object>();
        ArrayList<String> orders = new ArrayList<String>();
        orders.add(order);

        customerParams.put("limit", 50000);

        text("Looking for customer: " + token.getCard().getName());
        for (Customer customer : Customer.list(customerParams).getData()) {
            //text("Found customer: " + customer.getDescription());
            if (customer != null && customer.getSources() != null) {
                for (ExternalAccount extAcc : customer.getSources().getData()) {
                    if (extAcc instanceof Card) {
                        Card c = (Card) extAcc;
                        //text("Found card: " + c.getFingerprint() + " matching with: " + token.getCard().getFingerprint() + " with Name: " + token.getCard().getName());
                        if (c.getFingerprint().equals(token.getCard().getFingerprint())) {
                            if (token.getCard().getName().equalsIgnoreCase(c.getName())) {
                                text("Found matching card: " + token.getCard().getFingerprint());
                                card = c;
                                break;
                            }
                        }
                    }
                }
            }

            if (card != null) {
                Map<String, String> map = customer.getMetadata();
                String commaSeparated = map.get("orders");
                if (commaSeparated != null) {
                    orders.addAll(new ArrayList<String>(Arrays.asList(commaSeparated.split(","))));
                }
                map.put("orders", joinList(orders, ","));
                customer.setMetadata(map);
                customer.setDefaultCard(card.getId());
                return customer;
            }

        }

        text("Creating new customer: " + token.getCard().getName());
        customerParams = new HashMap<>();
        customerParams.put("source", token.getId());
        customerParams.put("description", token.getCard().getName());
        if (email != null && email.trim().length() > 6) {
            customerParams.put("email", email);
        }

        Map<String, String> initialMetadata = new HashMap<String, String>();
        initialMetadata.put("orders", joinList(orders, ","));
        initialMetadata.put("amount", String.valueOf(token.getAmount()));
        initialMetadata.put("currency", token.getCurrency());
        customerParams.put("metadata", initialMetadata);

        Customer newcustomer = Customer.create(customerParams);
        newcustomer.setDefaultCard(token.getCard().getId());
        Thread.sleep(300);
        return newcustomer;

    }

    private static void text(String txt) {
        System.out.println(txt);
    }

    public static String joinList(List list, String literal) {
        return list.toString().replaceAll(",", literal).replaceAll("[\\[.\\].\\s+]", "");
    }
}