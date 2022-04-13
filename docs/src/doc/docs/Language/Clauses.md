# Clauses

Clauses have the following grammar:

```kotlin

Subject(may | mayNot) { action(thing) } unless | asLongAs {
    circumstance1
    circumstance2
}
```

Allowance has the following semantics:

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

For more examples, please see the table below:

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
