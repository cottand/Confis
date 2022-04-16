---
icon: material/tab
---

# Parties, Things, and Actions

Much like real-life agreements have an appendix or a preface defining the terms used in the rest of the documents, Confis agreements require _declaring_ Parties (usually legal persons, real people, or groups of real people), Actions (stuff Parties can or cannot do), and Things (stuff the Actions can be performed on).

## Parties 

A Party is a participant in the agreement. It is up to the author to decide what a party 'can be', but in general they are a [legal person](https://www.law.cornell.edu/wex/legal_person), or perhaps a group of them (say, a team within a company).

Parties have names and can be named after the name of the variable (`alice`, in first example below), or the can be given full names and descriptions.

=== "Simple name Party"
    ```kotlin
    val alice by party
    ```

=== "Named Party"
    ```kotlin
    val alice by party(named = "Alice Liddell")
    ```

=== "Named Party with description "
    ```kotlin
    val alice by party(
        named = "Alice Liddell",
        description = "born 4 May 1852 in Westminser, London",
    )
    ```

## Actions

Actions constitute what a party can or cannot do. 
They can be performed on other Parties, or on Things.
They can also have simple names, full names, and descriptions.

=== "Simple name Action"
    ```kotlin
    val shareDataWith by action
    ```

=== "Named Action"
    ```kotlin
    val shareDataWith by action(named = "Sharing the dataset relating to customer data")
    ```

=== "Named Action with description "
    ```kotlin
    val shareDataWith by action(
        named = "Sharing the dataset relating to customer data",
        description = "as in copy and redistribute copies of the dataset",
    )
    ```


## Things

Things are what Actions are performed on. They can also carry descriptions and names:

=== "Simple name Thing"
    ```kotlin
    val licensingFee by thing
    ```

=== "Named Thing"
    ```kotlin
    val licensingFee by thing(named = "10€ licensing fee")
    ```

=== "Named Thing with description"
    ```kotlin
    val licensingFee by thing(
        named = "Licensing fee for customer data",
        description = "10€ in cash"
    )
    ```
