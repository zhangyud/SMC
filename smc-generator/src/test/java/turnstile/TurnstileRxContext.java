/*
 * ex: set ro:
 * DO NOT EDIT.
 * generated by smc (http://smc.sourceforge.net/)
 * from file : .sm
 */

package turnstile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TurnstileRxContext
    extends statemap.FSMContext
    implements java.io.Serializable
{
//---------------------------------------------------------------
// Member methods.
//

    public TurnstileRxContext(TurnstileRx owner)
    {
        this (owner, MainMap.Locked);
    }

    public TurnstileRxContext(TurnstileRx owner, TurnstileState initState)
    {
        super (initState);

        _owner = owner;
        _transitions = new TreeSet<>();

        _transitions.add("coin");
        _transitions.add("pass");
    }

    @Override
    public void enterStartState()
    {
        getState().entry(this);
        return;
    }

    public void coin(Double value)
    {
        _transition = "coin";
        getState().coin(this, value);
        _transition = "";
        return;
    }

    public void pass()
    {
        _transition = "pass";
        getState().pass(this);
        _transition = "";
        return;
    }

    public TurnstileState valueOf(int stateId)
        throws ArrayIndexOutOfBoundsException
    {
        return (_States[stateId]);
    }

    public TurnstileState getState()
        throws statemap.StateUndefinedException
    {
        if (_state == null)
        {
            throw(
                new statemap.StateUndefinedException());
        }

        return ((TurnstileState) _state);
    }

    protected TurnstileRx getOwner()
    {
        return (_owner);
    }

    public void setOwner(TurnstileRx owner)
    {
        if (owner == null)
        {
            throw (
                new NullPointerException(
                    "null owner"));
        }
        else
        {
            _owner = owner;
        }

        return;
    }

    public TurnstileState[] getStates()
    {
        return (_States);
    }

    public Set<String> getTransitions()
    {
        return (_transitions);
    }

    private void writeObject(java.io.ObjectOutputStream ostream)
        throws java.io.IOException
    {
        int size =
            (_stateStack == null ? 0 : _stateStack.size());
        int i;

        ostream.writeInt(size);

        for (i = 0; i < size; ++i)
        {
            ostream.writeInt(
                ((TurnstileState) _stateStack.get(i)).getId());
        }

        ostream.writeInt(_state.getId());

        return;
    }

    private void readObject(java.io.ObjectInputStream istream)
        throws java.io.IOException
    {
        int size;

        size = istream.readInt();

        if (size == 0)
        {
            _stateStack = null;
        }
        else
        {
            int i;

            _stateStack =
                new java.util.Stack<>();

            for (i = 0; i < size; ++i)
            {
                _stateStack.add(i, _States[istream.readInt()]);
            }
        }

        _state = _States[istream.readInt()];

        return;
    }

//---------------------------------------------------------------
// Member data.
//

    transient private TurnstileRx _owner;

    //-----------------------------------------------------------
    // Statics.
    //

    final Set<String> _transitions;
    transient private static TurnstileState[] _States =
    {
        MainMap.Locked,
        MainMap.Unlocked
    };

    //-----------------------------------------------------------
    // Constants.
    //

    private static final long serialVersionUID = 1L;

//---------------------------------------------------------------
// Inner classes.
//

    public static abstract class TurnstileState
        extends statemap.State
    {
    //-----------------------------------------------------------
    // Member methods.
    //

        public abstract Map<String, Integer> getTransitions();

        protected TurnstileState(String name, int id)
        {
            super (name, id);
        }

        protected void entry(TurnstileRxContext context) {}
        protected void exit(TurnstileRxContext context) {}

        protected void coin(TurnstileRxContext context, Double value)
        {
            Default(context);
        }

        protected void pass(TurnstileRxContext context)
        {
            Default(context);
        }

        protected void Default(TurnstileRxContext context)
        {
            throw (
                new statemap.TransitionUndefinedException(
                    "State: " +
                    context.getState().getName() +
                    ", Transition: " +
                    context.getTransition()));
        }

    //-----------------------------------------------------------
    // Member data.
    //
    }

    /* package */ static abstract class MainMap
    {
    //-----------------------------------------------------------
    // Member methods.
    //

    //-----------------------------------------------------------
    // Member data.
    //

        //-------------------------------------------------------
        // Constants.
        //

        public static final MainMap_Locked Locked =
            new MainMap_Locked("MainMap.Locked", 0);
        public static final MainMap_Unlocked Unlocked =
            new MainMap_Unlocked("MainMap.Unlocked", 1);
    }

    protected static class MainMap_Default
        extends TurnstileState
    {
    //-----------------------------------------------------------
    // Member methods.
    //

        @Override
        public Map<String, Integer> getTransitions()
        {
            return (_transitions);
        }

        protected MainMap_Default(String name, int id)
        {
            super (name, id);
        }

    //-----------------------------------------------------------
    // Member data.
    //

        //---------------------------------------------------
        // Statics.
        //

        private static Map<String, Integer> _transitions;

        static
        {
            _transitions = new HashMap<>();
            _transitions.put("coin", statemap.State.TRANSITION_UNDEFINED);
            _transitions.put("pass", statemap.State.TRANSITION_UNDEFINED);
        }

        //---------------------------------------------------
        // Constants.
        //

        private static final long serialVersionUID = 1L;
    }

    private static final class MainMap_Locked
        extends MainMap_Default
    {
    //-------------------------------------------------------
    // Member methods.
    //

        @Override
        public Map<String, Integer> getTransitions()
        {
            return (_transitions);
        }

        private MainMap_Locked(String name, int id)
        {
            super (name, id);
        }

        @Override
        protected void coin(TurnstileRxContext context, Double value)
        {
            TurnstileRx owner = context.getOwner();
            
            owner.isEnoughValue(value).
              subscribe(bool -> {
                if(bool) {
                  (context.getState()).exit(context);
                  context.clearState();
                  owner.unlock();
                  context.setState(MainMap.Unlocked);
                  (context.getState()).entry(context);
                } else {

                }
              }, e -> {
              
              });
            /*
            if (owner.isEnoughValue(value))
            {
                
            }
            else if (owner.isEnoughValue(value))
            {
                (context.getState()).exit(context);
                // No actions.
                context.setState(MainMap.Locked);
                (context.getState()).entry(context);
            }            else
            {
                super.coin(context, value);
            }*/

            return;
        }

        @Override
        protected void pass(TurnstileRxContext context)
        {
            TurnstileRx owner = context.getOwner();

            TurnstileState endState = context.getState();
            context.clearState();
            owner.alarm();
            context.setState(endState);
            return;
        }

    //-------------------------------------------------------
    // Member data.
    //

        //---------------------------------------------------
        // Statics.
        //

        private static Map<String, Integer> _transitions;

        static
        {
            _transitions = new HashMap<>();
            _transitions.put("coin", statemap.State.TRANSITION_DEFINED_LOCALLY);
            _transitions.put("pass", statemap.State.TRANSITION_DEFINED_LOCALLY);
        }

        //---------------------------------------------------
        // Constants.
        //

        private static final long serialVersionUID = 1L;
    }

    private static final class MainMap_Unlocked
        extends MainMap_Default
    {
    //-------------------------------------------------------
    // Member methods.
    //

        @Override
        public Map<String, Integer> getTransitions()
        {
            return (_transitions);
        }

        private MainMap_Unlocked(String name, int id)
        {
            super (name, id);
        }

        @Override
        protected void coin(TurnstileRxContext context, Double value)
        {
            TurnstileRx owner = context.getOwner();

            TurnstileState endState = context.getState();
            context.clearState();
            owner.thankyou();
            context.setState(endState);
            return;
        }

        @Override
        protected void pass(TurnstileRxContext context)
        {
            TurnstileRx owner = context.getOwner();

            (context.getState()).exit(context);
            context.clearState();
            owner.lock();
            context.setState(MainMap.Locked);
            (context.getState()).entry(context);
            return;
        }

    //-------------------------------------------------------
    // Member data.
    //

        //---------------------------------------------------
        // Statics.
        //

        private static Map<String, Integer> _transitions;

        static
        {
            _transitions = new HashMap<>();
            _transitions.put("coin", statemap.State.TRANSITION_DEFINED_LOCALLY);
            _transitions.put("pass", statemap.State.TRANSITION_DEFINED_LOCALLY);
        }

        //---------------------------------------------------
        // Constants.
        //

        private static final long serialVersionUID = 1L;
    }
}

/*
 * Local variables:
 *  buffer-read-only: t
 * End:
 */