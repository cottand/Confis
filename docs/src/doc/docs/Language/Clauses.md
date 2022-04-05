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

The combination of existing clauses will determine the answer to a _"Can this happen"_ question. In particular, more a result that would come out as `Unspecified`, more specific clauses can be used to further narrow down the result.
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
            <td>Forbidden</td>
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
            <td>Allowed</td>
        </tr>
        <tr>
            <td>False</td>
            <td>Forbidden</td>
        </tr>
    </tbody>
</table>
