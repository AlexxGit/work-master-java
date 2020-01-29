package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import j2html.tags.ContainerTag;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.schema.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class MainXml {
    public static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);

    public static void main(String[] args) throws Exception {
        System.out.format("Hello MasterJava!");

        URL payloadURL = Resources.getResource(args[0]);
        String project = args[1];

        Set<User> users = parseXmlFile(project, payloadURL);
        final String s = outHtml(users, project, Paths.get("users.html"));
        System.out.println(s);
    }

    private static String outHtml(Set<User> users, String project, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            ContainerTag table = table().with(tr().with(th("FullName"), th("email")));
            users.forEach(u-> table.with(tr().with(td(u.getValue()), td(u.getEmail()))));
            table.attr("border","1");
            table.attr("cellpadding","8");
            table.attr("cellspacing","0");

            String out = html().with(
                    head().with(title(project+ " users")),
                    body().with(h1(project+ " users"), table)
            ).render();

            writer.write(out);
            return out;
        }
    }

    private static Set<User> parseXmlFile(String projectName, URL payloadURL) throws Exception {
        JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));

        try (InputStream is = payloadURL.openStream()) {
            Payload payload = JAXB_PARSER.unmarshal(is);
            Project project = payload.getProjects().getProject().stream()
                    .filter(pr -> pr.getName().equals(projectName))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project name"));

            Set<Project.Group> groups = new HashSet<>(project.getGroup());

            List<User> users = payload.getUsers().getUser();

            return users.stream()
                    .filter(u -> StreamEx.of(u.getGroupRefs().stream())
                            .findAny(groups::contains)
                            .isPresent())
                    .collect(Collectors.toCollection(() -> new TreeSet<>(USER_COMPARATOR)));
        }
    }
}
