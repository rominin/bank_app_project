package contracts.blocker

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name("shouldBlockOperation")
    description("Should block operation if amount > 100000")

    request {
        method POST()
        url("/check")
        headers {
            contentType(applicationJson())
        }
        body(
                operationType: "WITHDRAW",
                userId: "42",
                amount: 150000.00
        )
    }

    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body(
                blocked: true,
                reason: "Operation blocked: 150000.00 > 100000.00 RUB"
        )
    }
}