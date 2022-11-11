
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, String> {
    @Override
    public String handleRequest(Map<String, Object> event, Context context)
    {
        try{
            return "cinelight.fr: " + Runner.doCinelightLogic(false);
        }
        catch (Exception exception) {
            System.out.println("Got error : " + event.toString());
            return "NOT OK";
        }
    }
}
