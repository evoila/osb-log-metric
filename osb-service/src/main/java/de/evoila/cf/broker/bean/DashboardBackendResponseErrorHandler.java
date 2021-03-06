package de.evoila.cf.broker.bean;

import de.evoila.cf.broker.exception.DashboardBackendRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.Date;

@Component
public class DashboardBackendResponseErrorHandler
        implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
            throws IOException {

        return httpResponse.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        throw new DashboardBackendRequestException("DashboardBackendRequestException: Failed to request resource from Dashboard Backend.",
                httpResponse.getStatusCode(), new Date().getTime());
    }
}