package org.wycliffeassociates.resourcecontainer

val accessorListFilesTestCases = listOf(
    "" to 5,
    "." to 5,
    "content" to 4,
    "content/c01" to 1,
    "non-existing" to 0,
    "path_is_a_file.mp3" to 0
)
