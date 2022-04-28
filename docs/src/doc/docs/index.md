# About

> "O ye who believe! When ye deal with each other, in transactions involving future obligations in a fixed period of time, reduce them to writing."
*-- Holy Qur'an, 2:282*

**Confis** (In Spanish, slang for _trust_ and short for _confianzas_) is a framework for writing and representing legal agreements.

It includes its own language to write legal contracts and the ability to ask questions in order to allow parties to figure out their legal capabilities and responsibilities.

It is meant to be a generalisation of a [Ricardian Contract](https://en.wikipedia.org/wiki/Ricardian_contract).

## Quick example

Below is a simple license agreement where `alice` licenses some data to `bob` in exchange for a fee of £100, which must be paid sometime in the month of April 2022.

Additionally, `bob` may not use the data for commercial purposes.

```kotlin title="License Example"
val alice by party
val bob by party

val payLicenseFeeTo by action("Fee of £100")
val use by action("Use as in deriving statistics from")
val share by action

val data by thing

// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall be governed and construed in
    accordance with the law of England and Wales.
"""


bob must payLicenseFeeTo(alice) underCircumstances {
    within { (1 of April)..(30 of April) year 2022 }
}

alice must share(data) asLongAs {
    after { bob did payLicenseFeeTo(alice) }
}

bob may use(data) asLongAs {
    after { bob did payLicenseFeeTo(alice) }
}

bob mayNot use(data) asLongAs {
    with purpose Commercial
}
```

This example of a license highlights the following:

- Defining [Parties](Language/Declarations.md#parties) (`bob` and `alice`)

- Defining [Actions](Language/Declarations.md#actions) the Parties can make (here `use`, `share`, and `payLicenseFeeTo`)

- Defining [Things](Language/Declarations.md#things) that Actions can be performed on (`data`).

- [Permissions](Language/PermissionClauses.md), which allow limiting the space within which parties are allowed to operate.
If a party does not operate within this space, we can say they are _non-compliant_.

- [Requirements](Language/RequirementClauses.md), which allow adding further constraints to what a party must do in order to remain _compliant_.
