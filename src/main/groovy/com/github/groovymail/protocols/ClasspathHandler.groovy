package com.github.groovymail.protocols

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClasspathHandler extends URLStreamHandler {

    private static Logger logger = LoggerFactory.getLogger( ClasspathHandler.class )

    /** The classloader to find resources from. */
    private ClassLoader classLoader;

    public ClasspathHandler() {
        this.classLoader = getClass().getClassLoader();
    }

    public ClasspathHandler(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        logger.debug( "Loading {} using classloader {}", u.getPath(), classLoader )
        final URL resourceUrl = classLoader.getResource(u.getPath());
        return resourceUrl.openConnection();
    }

}
