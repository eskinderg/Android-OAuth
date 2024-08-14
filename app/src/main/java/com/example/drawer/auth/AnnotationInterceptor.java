package com.example.drawer.auth;

import com.example.drawer.Constants;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

public class AnnotationInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request(); // addAuthHeader(chain.request()); // chain.request();
        Invocation invocation;
        if(chain.request().tag(Invocation.class) != null){
            invocation = chain.request().tag(Invocation.class);

        } else {
            return chain.proceed(request);
        }

        for (Annotation annotation : containedOnInvocation(invocation)) {
           request = handleAnnotation(annotation, request);
        }

        return chain.proceed(request);
    }

    private Request handleAnnotation(Annotation annotation, Request request) {
        if( annotation instanceof Authorized){
            return  addAuthHeader(request);
        }
        return request;
    }

    private Request addAuthHeader(Request request){
        return request.newBuilder()
                .addHeader("Authorization", "Bearer " + Constants.ACCESS_TOKEN)
                .build();
    }

    private Set<Annotation> containedOnInvocation(Invocation invocation) {
        return Set.of(invocation.method().getAnnotations());
    }
}
