# Permission Clauses

It is common in legal agreement to restrict the things a party is (or is not) allowed to do.
In order to specify these, Confis uses **Permission clauses**, which combine `may` or `mayNot` with some circumstances, optionally specified by `asLongAs` or `unless`.

We can say a party is compliant (as far as permission clauses go) with the agreement **when they only do the things they have permission to do**. For more ways to define compliance, see [Requirement Clauses](RequirementClauses.md).

A typical requirement clause looks as follows:
```kotlin
// specifies that alice may only pay bob during the first week of June and with
// research purposes
alice may { pay(bob) } asLongAs { 
    with purpose Research
    within { (1 of June)..(7 of June) year 2022 }
}
```

## Syntax
Permission clauses have the following grammar:

```kotlin
subject may/mayNot action(thing)
// or
subject may/mayNot action(thing) unless/asLongAs {
    circumstance1
    ...
    circumstanceN
}
```
## Semantics

Permission has the following semantics:

There are 4 possible results to a _"Can this happen"_ question: `Allow`, `Forbid`, `Unspecified` (
the agreement just does not say) or `Depends` (circumstances are too general for a specific answer)

The combination of existing clauses will determine the answer to a _"Can this happen"_ question. In
particular, more a result that would come out as `Unspecified`, more specific clauses can be used to
further narrow down the result.
<table>
    <thead>
        <tr>
            <th>Clause</th>
            <th>Circumstance <code>C</code></th>
            <th>Action <code>A</code></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=2> may <code>A</code> asLongAs <code>C</code> </td>
            <td>True</td>
            <td>Allowed</td>
        </tr>
        <tr>
            <td>False</td>
            <td>Unspecified</td>
        </tr>
        <tr>
            <td rowspan=2> may <code>A</code> unless <code>C</code> </td>
            <td>True</td>
            <td>Unspecified</td>
        </tr>
        <tr>
            <td>False</td>
            <td>Allowed</td>
        </tr>
        <tr>
            <td rowspan=2> mayNot <code>A</code> asLongAs <code>C</code> </td>
            <td>True</td>
            <td>Forbidden</td>
        </tr>
        <tr>
            <td>False</td>
            <td>Unspecified</td>
        </tr>
        <tr>
            <td rowspan=2> mayNot <code>A</code> unless <code>C</code> </td>
            <td>True</td>
            <td>Unspecified</td>
        </tr>
        <tr>
            <td>False</td>
            <td>Forbidden</td>
        </tr>
    </tbody>
</table>

The idea is to follow a 'least surprise' principle, meaning that the contract allows and forbids
exactly as it reads. For example, 'Alice **may** _share data_ **unless** _with commercial purpose_'
means that Alice is allowed share the data in general, but is not necessarily forbidden from sharing
it with commercial purpose - she is just not explicitly allowed from doing so.

If we wish to stop alice from sharing data with commercial purpose, we also need the following
sentence 'Alice **may not** _share data_ **as long as** _with commercial purpose_'. We need both of
these sentences to specify, in english, that 'Alice is allowed to share data in general but is
forbidden from doing so with commercial purposes'.

This encourages contract authors to be very specific concerning the behaviour they wish to describe,
and allows contract readers to not need to make assumptions.

### Examples

<table>
    <thread>
        <tr>
            <th>Sentence</th>
            <th>Purpose</th>
            <th>Alice eat cake?</th>
        </tr>
    </thread>
<tbody>
        <tr>
            <td rowspan=3> Alice may eatCake unless with purpose Commercial </td>
            <td>Research</td>
            <td>Allowed</td>
        </tr>
        <tr>
            <td>Commercial</td>
            <td>Unspecified</td>
        </tr>
        <tr>
            <td><em>no purpose specified</em></td>
            <td>Depends</td>
        </tr>
        <tr>
            <td rowspan=3> Alice mayNot eatCake asLongAs with purpose Commercial </td>
            <td>Research</td>
            <td>Unspecified</td>
        </tr>
        <tr>
            <td>Commercial</td>
            <td>Forbidden</td>
        </tr>
        <tr>
            <td><em>no purpose specified</em></td>
            <td>Depends</td>
        </tr>
</tbody>
</table>


## Examples

### Alice has permission to pay Bob in May 2022 and only in May 2022

In this example, it is not enough to allow Alice to pay in May, we also want to
forbid her from paying Bob some other time.

Therefore, we need both a '_may ... asLongAs'_ clause and a _'mayNot ... unless'_ clause.

```kotlin
val alice by party
val bob by party
val pay by action

val may = (1 of May year 2022)..(30 of May year 2022)

alice may { pay(bob) } asLongAs { within { may } }
alice mayNot { pay(bob) } unless { within { may } }
```

[//]: # (TODO more examples)
