package org.wycliffeassociates.resourcecontainer

val accessorListFilesTestCases = listOf(
    "" to 5,
    "." to 5,
    "content" to 4,
    "content/c01" to 1,
    "non-existing" to 0,
    "path_is_a_file.mp3" to 0
)

/** The accessor should be able to normalize and accept these variants of file path */
val filePathVariantTestCases = listOf(
    "./LICENSE.md",
    "LICENSE.md",
    "content/01/01.usfm",
    "./content/01/01.usfm"
)