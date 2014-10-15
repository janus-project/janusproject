
RELEASE JANUS
=============

The steps for releasing SARL are:

1) Remove "-SNAPSHOT" in all the poms.

2) Compiling locally without error.

    $> mvn clean install

3) Prepare the bundles for Maven Central:

    $> ./scripts/prepare-bundles-for-central

4) Tag the Git with the version number.

    $> git tag "vW.X.Y.Z"

5) Commit and push to Github:

    $> git commit
    $> git push --all

6) On Hudson, launch a build for updating the maven repositories and the Eclipse update sites.

7) Updload the Maven Bundle on Maven Central with [http://oss.sonatype.org](http://oss.sonatype.org)

8) Revert step 1.

9) Compiling locally without error.

    $> mvn clean install

10) Commit and push to Github:

    $> git commit
    $> git push --all

11) On Hudson, launch a build for updating the maven repositories and the Eclipse update sites.

12) Announce the new version on the mailing lists.

