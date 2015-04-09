
RELEASE JANUS
=============

The steps for releasing JANUS are:

1) Remove "-SNAPSHOT" in all the poms.

2) Remove any repository related to SARL or Janus in the POM (mandatory for Maven central).

3) Compiling locally without error.

    $> mvn clean install

4) Generate the aggregated API documentation:

    $> mvn-javadoc-aggregate

   or

    $> mvn -Dmaven.test.skip=true clean org.arakhne.afc.maven:tag-replacer:generatereplacesrc javadoc:aggregate

5) Prepare the bundles for Maven Central:

    $> ./scripts/prepare-bundles-for-central

6) Tag the Git with the version number.

    $> git tag "vX.Y.Z"

7) Commit and push to Github:

    $> git commit
    $> git push --all

8) On Hudson, launch a build for updating the maven repositories and the Eclipse update sites.
   If failing, revert 5, fix the problem, and go back to 3.

9) Copy the generated "aggregated" Javadoc on the Janus website.

10) Updload the Maven Bundle on Maven Central with [http://oss.sonatype.org](http://oss.sonatype.org)

11) Close the milestone on Github.

12) Add release notes on Github (from the Changes page on the website), attached to the release tag.

13) Revert steps 1 and 2.

14) Compiling locally without error.

    $> mvn clean install

15) Commit and push to Github:

    $> git commit
    $> git push --all

16) On Hudson, launch a build for updating the maven repositories and the Eclipse update sites.

17) Announce the new version on the mailing lists.

