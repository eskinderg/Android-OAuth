package app.mynote.core.utils;

import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.HttpDate;

public class TimeCalibrationInterceptor implements Interceptor {
    long minResponseTime = Long.MAX_VALUE;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = SystemClock.elapsedRealtime();
        Response response = chain.proceed(request);
        long responseTime = (SystemClock.elapsedRealtime() - startTime) / 2;

        Headers headers = response.headers();
        calibration(responseTime, headers);
        return response;
    }

    private void calibration(long responseTime, Headers headers) {
        if (headers == null) {
            return;
        }

        // If the current response time is less than the previous min one, calibrate again
        if (responseTime >= minResponseTime) {
            return;
        }

        String standardTime = headers.get("Date");
        if (!TextUtils.isEmpty(standardTime)) {
            Date parse = HttpDate.parse(standardTime);
            if (parse != null) {
                TimeManager.getInstance().initServerTime(parse.getTime() + responseTime);
                minResponseTime = responseTime;
            }
        }
    }
}
