project(est NONE)

add_subdirectory(setup)
add_subdirectory(shared/conf)
add_subdirectory(${APP_SERVER})

# build pki-est
javac(pki-est-classes
    DEPENDS
        pki-cmsutil-jar pki-certsrv-jar pki-cms-jar
    SOURCES
        src/main/java/*.java
    CLASSPATH
        ${JAXB_API_JAR}
        ${SLF4J_API_JAR}
        ${COMMONS_CLI_JAR}
        ${COMMONS_CODEC_JAR}
        ${COMMONS_IO_JAR}
        ${COMMONS_LANG3_JAR}
        ${COMMONS_NET_JAR}
        ${JACKSON2_CORE_JAR} ${JACKSON2_DATABIND_JAR}
        ${JACKSON2_ANNOTATIONS_JAR}
        ${JSS_JAR} ${JSS_SYMKEY_JAR}
        ${LDAPJDK_JAR}
        ${SERVLET_JAR}
        ${TOMCAT_CATALINA_JAR}
        ${TOMCATJSS_JAR}
        ${JAXRS_API_JAR} ${RESTEASY_JAXRS_JAR}
        ${PKI_CMSUTIL_JAR} ${PKI_TOMCAT_JAR} ${PKI_CERTSRV_JAR} ${PKI_CMS_JAR}
    OUTPUT_DIR
        ${CMAKE_CURRENT_BINARY_DIR}/classes
)

configure_file(
    ${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources/META-INF/MANIFEST.MF
    ${CMAKE_CURRENT_BINARY_DIR}/MANIFEST.MF
)

jar(pki-est-jar
    DEPENDS
        pki-est-classes
    CREATE
        ${CMAKE_BINARY_DIR}/dist/pki-est.jar
    OPTIONS
        m
    PARAMS
        ${CMAKE_CURRENT_BINARY_DIR}/MANIFEST.MF
    INPUT_DIR
        ${CMAKE_CURRENT_BINARY_DIR}/classes
)

# Create links at /usr/share/pki/est/webapps/est/admin/console.
# Create links in /usr/share/pki/est/webapps/est/WEB-INF/lib.
# This can be customized for different platforms in RPM spec.

add_custom_target(pki-est-links ALL
    COMMENT "Creating links for EST")

add_custom_command(
    TARGET pki-est-links
    COMMAND ${CMAKE_COMMAND} -E make_directory webapp/admin
    COMMAND ln -sf ${DATA_INSTALL_DIR}/server/webapps/est/admin/console ${CMAKE_CURRENT_BINARY_DIR}/webapp/admin
    COMMAND ${CMAKE_COMMAND} -E make_directory webapp/lib
    COMMAND ${CMAKE_COMMAND} -E create_symlink ${SLF4J_API_JAR} webapp/lib/slf4j-api.jar
    COMMAND ${CMAKE_COMMAND} -E create_symlink ${SLF4J_JDK14_JAR} webapp/lib/slf4j-jdk14.jar
    COMMAND ln -sf /usr/share/java/pki/pki-certsrv.jar ${CMAKE_CURRENT_BINARY_DIR}/webapp/lib/pki-certsrv.jar
    COMMAND ln -sf /usr/share/java/pki/pki-cms.jar ${CMAKE_CURRENT_BINARY_DIR}/webapp/lib/pki-cms.jar
    COMMAND ln -sf /usr/share/java/pki/pki-cmsbundle.jar ${CMAKE_CURRENT_BINARY_DIR}/webapp/lib/pki-cmsbundle.jar
    COMMAND ln -sf /usr/share/java/pki/pki-est.jar ${CMAKE_CURRENT_BINARY_DIR}/webapp/lib/pki-est.jar
)

install(
    FILES
        ${CMAKE_BINARY_DIR}/dist/pki-est.jar
    DESTINATION
        ${JAVA_JAR_INSTALL_DIR}/pki
)

set(PKI_EST_JAR ${CMAKE_BINARY_DIR}/dist/pki-est.jar CACHE INTERNAL "pki-est jar file")

# install directories
install(
    DIRECTORY
        shared/
    DESTINATION
        ${SHARE_INSTALL_PREFIX}/${APPLICATION_NAME}/${PROJECT_NAME}
    PATTERN
        "CMakeLists.txt" EXCLUDE
)

install(
    FILES
        ${CMAKE_CURRENT_BINARY_DIR}/webapp/admin/
    DESTINATION
        ${DATA_INSTALL_DIR}/est/webapps/est/admin
)

install(
    DIRECTORY
        ${CMAKE_CURRENT_BINARY_DIR}/webapp/lib/
    DESTINATION
        ${DATA_INSTALL_DIR}/est/webapps/est/WEB-INF/lib
)

# install empty directories
install(
    DIRECTORY
    DESTINATION
        ${VAR_INSTALL_DIR}/lock/pki/est
)

install(
    DIRECTORY
    DESTINATION
        ${VAR_INSTALL_DIR}/run/pki/est
)