# Index

**Confis** is a framework for representing legal agreements.

It includes its own language to write legal contracts and the ability to ask questions in order to allow parties to figure out their legal capabilities.

## Quick example

```kotlin

val alice by party("alice")

val bob by party("bob")

val pay by action

// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall be governed and construed in
    accordance with the law of England and Wales.
"""

alice may { pay(bob) } asLongAs {
    with purpose (Research)
}

alice mayNot { pay(bob) } asLongAs {
    with purpose Commercial
}
```
