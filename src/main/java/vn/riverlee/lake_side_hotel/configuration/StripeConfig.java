package vn.riverlee.lake_side_hotel.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
}