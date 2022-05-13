title = "SUB-LICENCE FOR SEISMIC DATA"

val licensee by party("the Licensee", description = "Oil & Gas Ltd")
val controller by party("the Controller", description = "The Controller of Her Majesty's Stationery Office")
val library by party("the Library", description = "UK Onshore Geophysical Library")

val data by thing("the Data", description = "seismic data listed in the Schedule")
val licence by thing("this Licence")
val nda by thing("a confidentiality agreement")

val provideServicesWith by action(
    "provide any services by using",
    description = "as in providing any service to any third party"
)
val transfer by action
val sell by action
val adapt by action(
    named = "copy or adapt",
    description = "as in deriving data and statistics from, copying, or distributing"
)
val thirdParty by party("a 3rd party", description = "not employed by $licensee")
val access by action
val agreeTo by action("agree to")



introduction = """Seismic data acquired pursuant to operations conducted subject to the Petroleum Act 1998 and
    landward area regulations made in exercise of powers conferred thereby are Crown Copyright
    material. $library, pursuant to worldwide exclusive rights granted by $controller hereby
    grants $licensee a non-exclusive Licence to use $data on the following terms
"""

licensee mayNot transfer(licence) unless {
    with consentFrom library
}

licensee mayNot sell(data)
licensee mayNot provideServicesWith(data)

thirdParty may access(data) asLongAs {

    with consentFrom licensee
    after { thirdParty did agreeTo(licence) }
    after  { thirdParty did agreeTo(nda) }
}

licensee mayNot adapt(data) unless  {
    with purpose Internal
    with consentFrom library
}

val builder: CircumstanceBuilder.() -> Unit = {

}
