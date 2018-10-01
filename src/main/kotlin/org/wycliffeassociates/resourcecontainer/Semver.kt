package org.wycliffeassociates.resourcecontainer

/**
 * This utility compares variable length semver styled strings
 * 0.1.0, 10.0.1, 1.0, 1, 1.2.3.4
 *
 * All non-numeric characters will be removed. e.g. v1.0 will become 1.0
 * 1.0-alpha.1 will become 1.0.1
 */

internal object Semver {

    /**
     * Checks if v1 is greater than v2
     * @param v1
     * @param v2
     * @return
     */
    fun gt(v1: String, v2: String): Boolean {
        return compare(v1, v2) == 1
    }

    /**
     * Checks if v1 is less than v2
     * @param v1
     * @param v2
     * @return
     */
    fun lt(v1: String, v2: String): Boolean {
        return compare(v1, v2) == -1
    }

    /**
     * Checks if v1 is equal to v2
     * @param v1
     * @param v2
     * @return
     */
    fun eq(v1: String, v2: String): Boolean {
        return compare(v1, v2) == 0
    }

    /**
     * Compares two version strings.
     * -1 v1 is less than v2
     * 0 both are equal
     * 1 v1 is greater than v2
     *
     * @param v1 the first string to compare
     * @param v2 the second string to compare
     * @return the comparison result
     */
    fun compare(v1: String, v2: String): Int {
        val ver1 = Version(v1)
        val ver2 = Version(v2)

        val max = Math.max(ver1.size(), ver2.size())
        for (i in 0 until max) {
            if (ver1.isWild(i) || ver2.isWild(i)) continue
            if (ver1[i] > ver2[i]) return 1
            if (ver1[i] < ver2[i]) return -1
        }
        return 0
    }

    /**
     * Utility for pumping values from a version string
     */
    private class Version(v: String) {
        private val slices: Array<String>

        init {
            slices = v.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }

        fun size(): Int {
            return slices.size
        }

        /**
         * Returns the value at the given semver index
         * @param index the position in the version
         * @return the integer value of the version position
         */
        operator fun get(index: Int): Int {
            if (index >= 0 && index < slices.size) {
                val value = clean(slices[index])
                return Integer.parseInt(value)
            } else {
                return 0
            }
        }

        /**
         * Checks if the value at the index is an asterisk (wild card)
         * @param index the position in the version
         * @return true if the value is a wildcard
         */
        fun isWild(index: Int): Boolean {
            var index = index
            if (index >= slices.size) index = slices.size - 1
            if (index < 0) index = 0

            return if (slices.size > 0) {
                clean(slices[index]) == "*"
            } else {
                false
            }
        }

        /**
         * Removes all non-numeric characters except for an asterisk.
         * @param val the value to be cleaned
         * @return a cleaned value
         */
        private fun clean(`val`: String): String {
            val cleaned = `val`.replace("[^\\d\\*]".toRegex(), "").trim { it <= ' ' }
            return if (cleaned.isEmpty()) {
                "0"
            } else {
                cleaned
            }
        }
    }
}