/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.septima;

import com.septima.script.AlreadyPublishedException;
import com.septima.script.HasPublished;
import com.septima.script.Scripts;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.runtime.JSType;

/**
 * A client session
 *
 * <p>
 * This object is created to represent a session with successfully authenticated
 * client. It is used to associate various resources such as tasks with a
 * client. Whenever a session is <code>cleanup()</code>-ed, the resources are
 * deleted.</p> Method rollback of database client is also invoked.
 *
 * @author pk, mg refactoring
 */
public class Session implements HasPublished {

    protected JSObject published;
    //
    private final String sessionId;
    private final Map<String, JSObject> modules = new HashMap<>();
    private Scripts.Space space;
    private PlatypusPrincipal principal;

    /**
     * Creates a new session with given session id.
     *
     * @param aSessionId unique session id.
     */
    public Session(String aSessionId) {
        super();
        sessionId = aSessionId;
    }

    public Scripts.Space getSpace() {
        return space;
    }

    public void setSpace(Scripts.Space aValue) {
        space = aValue;
    }

    public void setPrincipal(PlatypusPrincipal aValue) {
        principal = aValue;
    }

    /**
     * Deletes all resources belonging to this session.
     */
    public void cleanup() {
        // server modules
        modules.clear();
        // data in client's transaction
    }

    /**
     * Returns the creation time of this session (server time).
     *
     * @return session creation time.
     */
    public long getCTime() {
        return ctime.get();
    }

    /**
     * Returns the last access time of this session (server time).
     *
     * <p>
     * The last access time is the last time accessed() was called. This
     * mechanism is used to track down sessions which have been idle for a long
     * time, i.e. possible zombies.</p>
     *
     * @return last access time.
     */
    public long getATime() {
        return atime.get();
    }

    /**
     * Mark that this session was just accessed by its client, update last
     * access time.
     *
     * <p>
     * The last access time is the last time accessed() was called. This
     * mechanism is used to track down sessions which have been idle for a long
     * time, i.e. possible zombies.</p>
     *
     * <p>
     * Call this method once for each client request inside this session.</p>
     *
     * @return new last access time.
     */
    public long accessed() {
        atime.set(System.currentTimeMillis());
        return atime.get();
    }

    /*
     public void setPrincipal(PlatypusPrincipal aPrincipal) {
     String oldUserName = principal != null ? principal.getName() : null;
     String newUserName = aPrincipal != null ? aPrincipal.getName() : null;
     if (oldUserName == null ? newUserName != null : !oldUserName.equals(newUserName)) {
     userContext = null;
     if (newUserName != null && application != null && application.getDatabases() != null) {
     try {
     Map<String, String> userProps = DatabasesClient.getUserProperties(application.getDatabases(), newUserName, null, null);
     userContext = userProps.get(ClientConstants.F_USR_CONTEXT);
     } catch (Exception ex) {
     Logger.getLogger(Sessions.class.getName()).log(Level.WARNING, "Could not get user {0} properties (USR_CONTEXT, etc).", newUserName);
     }
     }
     }
     if (principal != null) {
     principal.setContext(null);
     }
     principal = aPrincipal;
     if (principal != null) {
     // let's update pricipal's 
     principal.setContext(userContext);
     // pricipal's roles are processed by container, so there is no need to update them here
     }
     }
     */
    /**
     * Returns server module by name.
     *
     * @param aName
     * @return
     */
    public JSObject getModule(String aName) {
        return modules.get(aName);
    }

    public boolean containsModule(String aName) {
        return modules.containsKey(aName);
    }

    public void registerModule(String aName, JSObject aModule) {
        if (aName == null || aName.isEmpty()) {
            JSObject c = (JSObject) aModule.getMember("constructor");
            aName = JSType.toString(c.getMember("name"));
        }
        modules.put(aName, aModule);
    }

    public void unregisterModule(String aModuleName) {
        modules.remove(aModuleName);
    }

    public void unregisterModules() {
        modules.clear();
    }

    public Set<Map.Entry<String, JSObject>> getModulesEntries() {
        return Collections.unmodifiableSet(modules.entrySet());
    }

    /**
     * Returns this session's id.
     *
     * @return session id.
     */
    public String getId() {
        return sessionId;
    }

    public boolean isNew() {
        return false;
    }

    @Override
    public void setPublished(JSObject aValue) {
        if (published != null) {
            throw new AlreadyPublishedException();
        }
        published = aValue;
    }

    @Override
    public JSObject getPublished() {
        return published;
    }
}
