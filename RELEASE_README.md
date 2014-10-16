
RELEASE JANUS
=============

The steps for releasing SARL are:

1) Remove "-SNAPSHOT" in all the poms.

2) Compiling locally without error.

    $> mvn clean install

3) Generate the aggregated API documentation:

    $> 

4) Prepare the bundles for Maven Central:

    $> ./scripts/prepare-bundles-for-central

5) Tag the Git with the version number.

    $> git tag "vW.X.Y.Z"

6) Commit and push to Github:

    $> git commit
    $> git push --all

7) On Hudson, launch a build for updating the maven repositories and the Eclipse update sites.

8) Updload the Maven Bundle on Maven Central with [http://oss.sonatype.org](http://oss.sonatype.org)

9) Copy the Javadoc on the website.

10) Revert step 1.

11) Compiling locally without error.

    $> mvn clean install

12) Commit and push to Github:

    $> git commit
    $> git push --all

13) On Hudson, launch a build for updating the maven repositories and the Eclipse update sites.

14) Announce the new version on the mailing lists.

