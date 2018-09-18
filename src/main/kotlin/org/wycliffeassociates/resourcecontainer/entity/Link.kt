package org.wycliffeassociates.resourcecontainer.entity

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Represents a link to a resource container
 */

class Link {
    var title: String?
    val url: String?
    val resource: String?
    val project: String?
    val language: String?
    val arguments: String?
    val protocol: String?
    val chapter: String?
    val chunk: String?
    val lastChunk: String?

    /**
     * Checks if this is an external link
     * @return
     */
    val isExternal: Boolean
        get() = this.url != null

    /**
     * Checks if this is a media link
     * @return
     */
    val isMedia: Boolean
        get() = this.protocol != null

    /**
     * Checksk if this is a Bible passage link
     * @return
     */
    val isPassage: Boolean
        get() = this.chapter != null && this.chunk != null

    /**
     * Creates a simple external link
     * @param title the human readable title of the link
     * @param url the external link address
     */
    private constructor(title: String?, url: String?) {
        this.title = title
        this.url = url

        protocol = null
        resource = null
        project = null
        chapter = null
        chunk = null
        lastChunk = null
        arguments = null
        language = null
    }

    /**
     * Creates a new resource container link.
     *
     * @param protocol used to indicate if this is a media link
     * @param title the human readable title of the link
     * @param language the language of the linked resource container
     * @param project the project of the linked resource container
     * @param resource the resource of the linked resource container
     * @param arguments the raw arguments on the link
     * @param chapter the chapter in the linked resource container
     * @param chunk the chunk (first one if the arguments included a range of chunks) in the linked resource container
     * @param lastChunk the last chunk (if the arguments included a range of chunks) referenced by this link
     */
    private constructor(protocol: String?, title: String?, language: String?, project: String?, resource: String?, arguments: String?, chapter: String?, chunk: String?, lastChunk: String?) {
        this.url = null
        this.protocol = protocol
        this.title = title
        this.language = language
        this.project = project
        this.resource = resource
        this.arguments = arguments
        this.chapter = chapter
        this.chunk = chunk
        this.lastChunk = lastChunk
    }

    /**
     * Returns the formatted passage title e.g. 1:2-3
     * @return
     */
    fun passageTitle(): String? {
        if (isPassage) {
            var tail = ""
            if (lastChunk != null) tail = "-" + formatNumber(lastChunk)
            return formatNumber(chapter) + ":" + formatNumber(chunk) + tail
        }
        return null
    }

    /**
     * Attempts to format the string as a number (without leading 0's)
     * otherwise the original value will be returned.
     * @param value
     * @return
     */
    private fun formatNumber(value: String?): String {
        try {
            return Integer.parseInt(value!!).toString() + ""
        } catch (e: NumberFormatException) {
        }

        return value!!.trim { it <= ' ' }.toLowerCase()
    }

    companion object {

        /**
         * Parses a link. This could be an external link or a resource container link
         *
         * @param link
         * @throws Exception if the link is invalid
         */
        @Throws(Exception::class)
        fun parseLink(link: String): Link? {
            val anonymousPattern = Pattern.compile("\\[\\[([^\\]]*)\\]\\]", Pattern.DOTALL)
            val titledPattern = Pattern.compile("\\[([^\\]]*)\\]\\(([^\\)]*)\\)", Pattern.DOTALL)

            var linkTitle: String? = null
            var linkPath: String? = link
            var m: Matcher
            var numMatches = 1

            // find anonymous links
            m = anonymousPattern.matcher(link)
            while (m.find()) {
                if (numMatches > 1) throw Exception("Invalid link! Multiple links found")
                numMatches++
                linkPath = m.group(1).toLowerCase()
            }

            // find titled links
            m = titledPattern.matcher(link)
            numMatches = 1
            while (m.find()) {
                if (numMatches > 1) throw Exception("Invalid link! Multiple links found")
                numMatches++
                linkTitle = m.group(1)
                linkPath = m.group(2).toLowerCase()
            }

            // process link path
            return if (linkPath != null) {
                // external link
                if (linkPath.startsWith("http")) {
                    Link(linkTitle, linkPath)
                } else parseResourceLink(linkTitle, linkPath)
            } else null

        }

        /**
         * Parses a resource container link
         * @param title
         * @param path
         * @return
         */
        @Throws(Exception::class)
        private fun parseResourceLink(title: String?, path: String): Link? {
            var title = title
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

            val components = path.split("\\/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

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
            title = nullEmpty(title)
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
            } else null
        }


        /**
         * Returns the value if it is not empty otherwise null
         * @param value
         * @return
         */
        private fun nullEmpty(value: String?): String? {
            return if (value != null && value.isEmpty()) null else value
        }

        /**
         * Returns a list of links found in the text.
         * This is used to turn inline Bible passages into links.
         * The returned links will include their position within the charsequence
         *
         * @param text the text that will be searched for Bible passages
         * @return
         */
        fun findLinks(text: CharSequence): List<Link>? {
            // TODO: 10/11/16 automatically parse bible passages.
            return null
        }
    }
}
