package org.wycliffeassociates.resourcecontainer.entity

import java.io.InputStream

class Content(val project: Project, val streams: Map<String, InputStream>)