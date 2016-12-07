/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.link.Requester;
import org.dsa.iot.dslink.node.value.SubscriptionValue;
import org.dsa.iot.dslink.util.handler.Handler;

/**
 * Needed because the SDK can't handle multiple subscribers for the same path.
 *
 * @author Aaron Hansen
 */
class SubscriptionManager {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private AlarmService service;
    private Map<String, SubscriptionFacade> subscriptions = new HashMap<>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    SubscriptionManager(AlarmService service) {
        this.service = service;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    private Requester getRequester() {
        DSLink link = service.getLinkHandler().getRequesterLink();
        if (link != null) {
            return link.getRequester();
        }
        return null;
    }

    public synchronized void subscribe(final String path,
                                       Handler<SubscriptionValue> handler) {
        SubscriptionFacade facade = subscriptions.get(path);
        if (facade == null) {
            facade = new SubscriptionFacade(handler);
            subscriptions.put(path, facade);
            AlarmUtil.enqueue(new Runnable() {
                @Override
                public void run() {
                    subscribePath(path);
                }
            });
        } else {
            facade.add(handler);
        }
    }

    /**
     * Asynchronously called by subscribe to perform the actual subscription with the
     * requester.
     */
    private void subscribePath(String path) {
        try {
            SubscriptionFacade facade = null;
            synchronized (this) {
                facade = subscriptions.get(path);
            }
            if (facade == null) {
                return;
            }
            Requester requester = getRequester();
            if (requester != null) {
                requester.subscribe(path, facade);
            }
        } catch (Exception x) {
            AlarmUtil.logError(path, x);
        }
    }

    public synchronized void unsubscribe(final String path,
                                         Handler<SubscriptionValue> handler) {
        SubscriptionFacade facade = subscriptions.get(path);
        facade.remove(handler);
        if (facade.size() == 0) {
            subscriptions.remove(path);
            if (service.getLinkHandler().getRequesterLink().isConnected()) {
                AlarmUtil.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        unsubscribePath(path);
                    }
                });
            }
        }
    }

    /**
     * Asynchronously called by unsubscribe to perform the actual unsubscription with the
     * requester.
     */
    private void unsubscribePath(String path) {
        try {
            Requester requester = getRequester();
            if (requester != null) {
                requester.unsubscribe(path, null);
            }
        } catch (Exception x) {
            AlarmUtil.logError(path, x);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Acts as a handler for multiple subscriptions to the same path.
     */
    private static class SubscriptionFacade implements Handler<SubscriptionValue> {

        private Handler<SubscriptionValue> first;
        private HashSet<Handler<SubscriptionValue>> handlers;

        public SubscriptionFacade(Handler<SubscriptionValue> first) {
            this.first = first;
        }

        /**
         * Add the handler to the internal collection.
         */
        public synchronized void add(Handler<SubscriptionValue> handler) {
            if (handlers == null) {
                handlers = new HashSet<>();
                handlers.add(first);
                first = null;
            }
            if (!handlers.contains(handler)) {
                handlers.add(handler);
            }
        }

        /**
         * Forwards the call to all handlers.
         */
        public synchronized void handle(SubscriptionValue value) {
            if (first != null) {
                first.handle(value);
            } else {
                for (Handler<SubscriptionValue> handler : handlers) {
                    handler.handle(value);
                }
            }
        }

        /**
         * Removes the handler to the internal collection.
         */
        public synchronized void remove(Handler<SubscriptionValue> handler) {
            if (first != null) {
                if (first == handler) {
                    first = null;
                }
            } else {
                handlers.remove(handler);
            }
        }

        /**
         * The number of handlers in the internal colleciton.
         */
        public synchronized int size() {
            if (first != null) {
                return 1;
            }
            if (handlers != null) {
                return handlers.size();
            }
            return 0;
        }
    }
}
