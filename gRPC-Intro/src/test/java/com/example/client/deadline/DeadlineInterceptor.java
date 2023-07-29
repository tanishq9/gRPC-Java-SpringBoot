package com.example.client.deadline;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Deadline;
import io.grpc.MethodDescriptor;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DeadlineInterceptor implements ClientInterceptor {
	@Override
	// method (which we are calling), callOptions (config related to gRPC call to be made), channel
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
		// return channel.newCall(methodDescriptor, callOptions);

		// deadline is set already in the call then do not override it
		Deadline deadline = callOptions.getDeadline();

		if (Objects.isNull(deadline)) {
			callOptions = callOptions.withDeadline(Deadline.after(1, TimeUnit.SECONDS));
		}

		return channel.newCall(methodDescriptor, callOptions);
	}
}
