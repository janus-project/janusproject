
RELEASE JANUS
=============

The steps for releasing JANUS are:

1) Remove "-SNAPSHOT" in all the poms.

2) Compiling locally without error.

    $> mvn clean install

3) Generate the aggregated API documentation:

    $> mvn-javadoc-aggregate

   or

    $> mvn -Dmaven.test.skip=true clean org.arakhne.afc.maven:tag-replacer:generatereplacesrc javadoc:aggregate

4) Prepare the bundles for Maven Central:

    $> ./scripts/prepare-bundles-for-central

5) Tag the Git with the version number.

    $> git tag "vX.Y.Z"

6) Commit and push to Github:

    $> git commit
    $> git push --all

7) On Hudson, launch a build for updating the maven repositories and the Eclipse update sites.
   If failing, revert 5, fix the problem, and go back to 3.

8) Copy the generated "aggregated" Javadoc on the Janus website.

9) Updload the Maven Bundle on Maven Central with [http://oss.sonatype.org](http://oss.sonatype.org)

10) Close the milestone on Github.

11) Add release notes on Github (from the Changes page on the website), attached to the release tag.

12) Revert steps 1 and 2.

13) Compiling locally without error.

    $> mvn clean install

14) Commit and push to Github:

    $> git commit
    $> git push --all

15) On Hudson, launch a build for updating the maven repositories and the Eclipse update sites.

16) Announce the new version on the mailing lists.

