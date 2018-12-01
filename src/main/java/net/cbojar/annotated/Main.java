package net.cbojar.annotated;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Main {
	private static final ClassLoader CLASSLOADER = Main.class.getClassLoader();

	public static void main(final String... args) throws URISyntaxException {
		if (!(CLASSLOADER instanceof URLClassLoader)) {
			System.err.println("Classloader is not a URL classloader");
			return;
		}

		final URL[] urls = ((URLClassLoader)CLASSLOADER).getURLs();
		for (final URL url : urls) {
			System.out.println(url);
			try {
				printAllClasses(urlToFile(url));
			} catch (final URISyntaxException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static File urlToFile(final URL url) throws URISyntaxException {
		return Paths.get(url.toURI()).toFile();
	}

	private static void printAllClasses(final File root) {
		final String rootPath = root.getPath();

		allFilesIn(root).stream()
			.map((file) -> file.getPath())
			.filter((path) -> path.endsWith(".class"))
			.map((path) -> path.substring(0, path.length() - ".class".length()))
			.map((path) -> path.substring(rootPath.length() + 1, path.length()))
			.map((path) -> path.replace('/', '.'))
			.map((path) -> classFor(path))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter((clazz) -> !clazz.isAnnotation())
			.filter((clazz) -> !clazz.isEnum())
			.filter((clazz) -> !clazz.isInterface())
			.filter((clazz) -> clazz.isAnnotationPresent(MyTypeAnnotation.class))
			.peek((clazz) -> System.out.println(classWithAnnotations(clazz)))
			.map((clazz) -> clazz.getMethods())
			.flatMap(Arrays::stream)
			.filter((method) -> method.isAnnotationPresent(MyMethodAnnotation.class))
			.map((method) -> methodWithAnnotations(method))
			.forEach(System.out::println);
	}

	private static List<File> allFilesIn(final File file) {
		if (file.isFile()) {
			return Collections.singletonList(file);
		} else if (file.isDirectory()) {
			final List<File> files = new ArrayList<>();

			for (final File subFile : file.listFiles()) {
				files.addAll(allFilesIn(subFile));
			}

			return files;
		} else {
			return Collections.emptyList();
		}
	}

	private static Optional<Class<?>> classFor(final String className) {
		try {
			return Optional.of(Class.forName(
				className, false, Main.class.getClassLoader()));
		} catch (final ClassNotFoundException ex) {
			return Optional.empty();
		}
	}

	private static String classWithAnnotations(final Class<?> clazz) {
		return String.format("%s: %s",
			clazz, Arrays.toString(clazz.getAnnotations()));
	}

	private static String methodWithAnnotations(final Method method) {
		return String.format("%s: %s",
			method, Arrays.toString(method.getAnnotations()));
	}
}