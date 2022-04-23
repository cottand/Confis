# Agreements As Documents

Agreements written as Confis code can be converted into [markdown](https://www.markdownguide.org/), which can easily be converted to a PDF or a simple web page.

Take the following example:


!!! note "`example.confis.kts` as Document"
    <br/>
    # Confis Agreement

    ## 1 - Parties

    1. Alice Liddell of Imperial College (**Alice**).
    2. Bob the Builder (**Bob**).

    ## 2 - Definitions

    1. _"the Data"_: Confidential non-anonymised customer data.
    1. _"pay"_.
    2. _"send"_: Email file to company inbox.

    ## 3 - Terms

    1. Alice must pay Bob:

        1. from 01/06/2022 to 07/06/2022 inclusive
    2. Bob must send the Data:

        1. only after Alice did pay Bob
        2. from 07/06/2022 to 14/06/2022 inclusive
    3. Alice may not pay Bob under the following circumstances:

        1. only after Alice did pay Bob
    4. The Licence and the terms and conditions thereof shall  be governed and construed in accordance with the law of England and Wales.


Which corresponds to the following Confis code:

```kotlin title="example.confis.kts"
val alice by party(
        named = "Alice",
        description = "Alice Liddell of Imperial College"
)

val bob by party(named = "Bob", description = "Bob the Builder")

val pay by action

val data by thing(
        named = "the Data",
        description = "Confidential non-anonymised customer data"
)

val send by action(description = "Email file to company inbox")

// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall 
    be governed and construed in accordance with the law
    of England and Wales.
"""


alice must pay(bob) underCircumstances {
    within { (1 of June)..(7 of June) year 2022 }
}

alice mayNot pay(bob) asLongAs {
    after { alice did pay(bob) }
}

bob must send(data) underCircumstances {
    after { alice did pay(bob) }
    within { (7 of June)..(14 of June) year 2022 }
}
```
