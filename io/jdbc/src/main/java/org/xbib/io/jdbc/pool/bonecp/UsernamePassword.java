
package org.xbib.io.jdbc.pool.bonecp;


import com.google.common.base.Objects;

/**
 * Composite key handle for datasource.
 *
 */
public class UsernamePassword {

    /**
     * Handle to store a username.
     */
    private String username;
    /**
     * Handle to store a password.
     */
    private String password;

    /**
     * Default constructor.
     *
     * @param username
     * @param password
     */
    public UsernamePassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username field.
     *
     * @return username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Returns the password field.
     *
     * @return password
     */
    public String getPassword() {
        return this.password;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UsernamePassword) {
            final UsernamePassword that = (UsernamePassword) obj;
            return Objects.equal(this.username, that.getUsername())
                    && Objects.equal(this.password, that.getPassword());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.username, this.password);
    }
}
