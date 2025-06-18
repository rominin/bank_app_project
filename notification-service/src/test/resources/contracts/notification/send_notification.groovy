package contracts.notification

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name("send_notification")
    description("Should accept a valid notification request")

    request {
        method 'POST'
        url '/notify'
        body(
                userName: "alice",
                message: "Your funds have been credited."
        )
        headers {
            contentType(applicationJson())
        }
    }

    response {
        status 200
    }
}