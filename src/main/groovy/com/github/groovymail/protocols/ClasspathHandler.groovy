package com.github.groovymail.protocols

class ClasspathHandler extends URLStreamHandler {
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
        final URL resourceUrl = classLoader.getResource(u.getPath());
        return resourceUrl.openConnection();
    }

}
