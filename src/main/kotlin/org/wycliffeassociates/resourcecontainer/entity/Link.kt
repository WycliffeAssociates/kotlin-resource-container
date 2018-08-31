package org.wycliffeassociates.resourcecontainer.entity

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.util.regex.Matcher
import java.util.regex.Pattern

data class Link(
        var title: String?,
        val url: String?,
        val resource: String? = null,
        val project: String? = null,
        val language: String? = null,
        val arguments: String? = null,
        val protocol: String? = null,
        val chapter: String? = null,
        val chunk: String? = null,
        val lastChunk: String? = null
) {

    /**
     * Checks if this is an external link
     * @return
     */
    inline fun isExternal(): Boolean = url != null

    /**
     * Checks if this is a media link
     * @return
     */
    inline fun isMedia(): Boolean = protocol != null

    /**
     * Checks if this is a Bible passage link
     * @return
     */
    inline fun isPassage(): Boolean = chapter != null && chunk != null

    /**
     * Returns the formatted passage title e.g. 1:2-3
     * @return
     */
    fun passageTitle(): String {
        if (isPassage()) {
            var tail = ""
            lastChunk?.let {
                tail = "-" + formatNumber(lastChunk)
            }
            return formatNumber(chapter) + ":" + formatNumber(chunk) + tail
        }
    }

    /**
     * Attempts to format the string as a number (without leading 0's)
     * otherwise the original value will be returned.
     * @param value
     * @return
     */
    private fun formatNumber(value: String): String {
        try {
            return Integer.parseInt(value).toString()
        } catch (e: NumberFormatException) {
        }
        return value.trim().toLowerCase()
    }


    companion object {

        /**
         * Parses a link. This could be an external link or a resource container link
         *
         * @param link
         * @throws Exception if the link is invalid
         */
        @Throws(Exception::class)
        fun parseLink(link: String): Link {
            val anonymousPattern = Pattern.compile("\\[\\[([^\\]]*)\\]\\]", Pattern.DOTALL)
            val titledPattern = Pattern.compile("\\[([^\\]]*)\\]\\(([^\\)]*)\\)", Pattern.DOTALL);

            var linkTitle: String? = null
            var linkPath = link
            var m: Matcher

            var numMatches = 1

            //find anonymous links
            m = anonymousPattern.matcher(link)
            while (m.find()) {
                if (numMatches > 1) {
                    throw Exception("Invalid link! Multiple links found")
                }
                numMatches++
                linkPath = m.group(1).toLowerCase()
            }

            //find titled links
            m = titledPattern.matcher(link)
            numMatches = 1
            while (m.find()) {
                if (numMatches > 1) {
                    throw Exception("Invalid link! Multiple links found")
                }
                numMatches++
                linkTitle = m.group(1)
                linkPath = m.group(2).toLowerCase()
            }

            //external link
            if (linkPath.startsWith("http")) {
                linkTitle.let {
                    return Link(linkTitle, linkPath)
                }
            }
            return parseResourceLink(linkTitle, linkPath)
        }


        /**
         * Parses a resource container link
         * @param title
         * @param path
         * @return
         */
        fun parseResourceLink(title: String?, path: String): Link {
            var path = path

            val pattern = Pattern.compile("^((\\w+):)?\\/?(.*)", Pattern.DOTALL)

            var protocol: String? = null
            var language: String? = null
            var project: String? = null
            var resource: String? = null
            var chapter: String? = null
            var chunk: String? = null
            var lastChunk: String? = null
            var arguments: String? = null


            // pull out the protocol
            // TRICKY: also pulls off the first / so our string splitting correctly finds the language
            val m = pattern.matcher(path)
            if (m.find()) {
                protocol = m.group(2)
                path = m.group(3)
            }

            val components = path.split("\\/").toTypedArray()

            // /chapter
            if (components.size == 1) arguments = components[0]

            // /language/project
            if (components.size > 1) {
                language = components[0]
                project = components[1]
            }

            // /language/project/resource
            if (components.size > 2) {
                language = components[0]
                project = components[1]
                resource = components[2]

                // TRICKY: resource can be skipped
                // /language/project/chapter:chunk
                if (resource.contains(":")) {
                    arguments = resource
                    resource = null
                }
            }

            // /language/project/resource/args
            if (components.size > 3) {
                language = components[0]
                project = components[1]
                resource = components[2]
                arguments = components[3]
                // re-build arguments that had the delimiter
                for (i in 4 until components.size - 1) {
                    arguments += "/" + components[i]
                }
            }

            // get chapter:chunk from arguments
            chapter = arguments
            if (arguments != null && arguments.contains(":")) {
                val bits = arguments.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                chapter = bits[0]
                chunk = bits[1]
            }

            // get last chunk
            if (chunk != null && chunk.contains("-")) {
                val bits = chunk.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                chunk = bits[0]
                lastChunk = bits[1]
            }

            // assume resource
            if (resource == null && project != null) resource = project

            // nullify empty strings
            protocol = nullEmpty(protocol)
            var title = nullEmpty(title)
            language = nullEmpty(language)
            project = nullEmpty(project)
            resource = nullEmpty(resource)
            arguments = nullEmpty(arguments)
            chapter = nullEmpty(chapter)
            chunk = nullEmpty(chunk)
            lastChunk = nullEmpty(lastChunk)

            // validate chunks
            if (chunk != null && chunk.contains(",") || lastChunk != null && lastChunk.contains(","))
                throw Exception("Invalid passage link $path")

            return if (project != null && resource != null || arguments != null) {
                Link(protocol, title, language, project, resource, arguments, chapter, chunk, lastChunk)
            } else throw Exception("Invalid Link! Could not parse resource link")
        }

        private inline fun nullEmpty(value: String?): String? = if (value.isNullOrEmpty()) null else value
    }
}
