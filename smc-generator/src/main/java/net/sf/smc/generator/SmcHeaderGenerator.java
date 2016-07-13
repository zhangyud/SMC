//
// The contents of this file are subject to the Mozilla Public
// License Version 1.1 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy
// of the License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
// implied. See the License for the specific language governing
// rights and limitations under the License.
//
// The Original Code is State Machine Compiler (SMC).
//
// The Initial Developer of the Original Code is Charles W. Rapp.
// Portions created by Charles W. Rapp are
// Copyright (C) 2005, 2008. Charles W. Rapp.
// All Rights Reserved.
//
// Contributor(s):
//   Eitan Suez contributed examples/Ant.
//   (Name withheld) contributed the C# code generation and
//   examples/C#.
//   Francois Perrad contributed the Python code generation and
//   examples/Python.
//   Chris Liscio contributed the Objective-C code generation
//   and examples/ObjC.
//
// RCS ID
// $Id: SmcHeaderGenerator.java,v 1.9 2014/09/13 06:25:32 fperrad Exp $
//
// CHANGE LOG
// (See the bottom of this file.)
//

package net.sf.smc.generator;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import net.sf.smc.model.SmcAction;
import net.sf.smc.model.SmcElement;
import net.sf.smc.model.SmcFSM;
import net.sf.smc.model.SmcMap;
import net.sf.smc.model.SmcParameter;
import net.sf.smc.model.SmcState;
import net.sf.smc.model.SmcTransition;
import net.sf.smc.model.SmcVisitor;

/**
 * Visits the abstract syntax tree emitting a C++ header file.
 * @see SmcElement
 * @see SmcVisitor
 * @see SmcCppGenerator
 * @see SmcOptions
 *
 * @author <a href="mailto:rapp@acm.org">Charles Rapp</a>
 */

public final class SmcHeaderGenerator
    extends SmcCodeGenerator
{
//---------------------------------------------------------------
// Member methods
//

    //-----------------------------------------------------------
    // Constructors.
    //

    /**
     * Creates a C++ header code generator for the given options.
     */
    public SmcHeaderGenerator(final SmcOptions options)
    {
        super (options, "h");
    } // end of SmcHeaderGenerator(SmcOptions)

    //
    // end of Constructors.
    //-----------------------------------------------------------

    //-----------------------------------------------------------
    // SmcVisitor Abstract Method Impelementation.
    //

    /**
     * Emits C++ header code for the finite state machine.
     * @param fsm emit C=+ header code for this finite state
     * machine.
     */
    public void visit(SmcFSM fsm)
    {
        String targetfileCaps;
        String packageName = fsm.getPackage();
        String context = fsm.getContext();
        String fsmClassName = fsm.getFsmClassName();
        String mapName;
        List<SmcTransition> transList;
        String separator;
        List<SmcParameter> params;
        Iterator<SmcParameter> pit;
        int packageDepth = 0;

        _source.println("//");
        _source.println("// ex: set ro:");
        _source.println("// DO NOT EDIT.");
        _source.println("// generated by smc (http://smc.sourceforge.net/)");
        _source.print("// from file : ");
        _source.print(_srcfileBase);
        _source.println(".sm");
        _source.println("//");
        _source.println();

        // The first two lines in the header file should be:
        //
        //    #ifndef <source file name>_H
        //    #define <source file name>_H
        //
        // where the source file name is all in caps.
        // The last line is:
        //
        //    #endif
        //

        // Make the file name upper case and replace
        // slashes with underscores.
        targetfileCaps = _targetfileBase.replace('\\', '_');
        targetfileCaps = targetfileCaps.replace('/', '_');
        targetfileCaps = targetfileCaps.toUpperCase();
        _source.print("#ifndef ");
        _source.print(targetfileCaps);
        _source.println("_H");
        _source.print("#define ");
        _source.print(targetfileCaps);
        _source.println("_H");
        _source.println();

        // If this application *is* using iostreams to output
        // debug messages, then define SMC_USES_IOSTREAMS.
        // Otherwise the user is responsible for providing a
        // TRACE macro to output the debug messages.
        if (_noStreamsFlag == false)
        {
            _source.println();
            _source.println("#define SMC_USES_IOSTREAMS");
        }

        // If this application is *not* using exceptions, then
        // define SMC_NO_EXCEPTIONS.
        if (_noExceptionFlag == true)
        {
            _source.println();
            _source.println("#define SMC_NO_EXCEPTIONS");
        }

        // Include required standard .h files.
        _source.println();
        _source.println("#include <statemap.h>");

        _source.println();

        // If a namespace was specified, then output that
        // namespace now. If the package name is "a::b::c", then
        // this must be converted to:
        // namespace a {
        //   namespace b {
        //     namespace c {
        //       ...
        //     }
        //   }
        // }
        _indent = "";
        if (packageName != null && packageName.length() > 0)
        {
            StringTokenizer tokenizer =
                new StringTokenizer(packageName, "::");
            String token;

            while (tokenizer.hasMoreTokens() == true)
            {
                token = tokenizer.nextToken();
                ++packageDepth;

                _source.print(_indent);
                _source.print("namespace ");
                _source.println(token);
                _source.print(_indent);
                _source.println("{");
                _indent += "    ";
            }
        }

        // Forward declare all the state classes in all the maps.
        _source.print(_indent);
        _source.println("// Forward declarations.");
        for (SmcMap map: fsm.getMaps())
        {
            mapName = map.getName();

            // class <map name>;
            _source.print(_indent);
            _source.print("class ");
            _source.print(mapName);
            _source.println(";");

            // Iterate over the map's states.
            for (SmcState state: map.getStates())
            {
                _source.print(_indent);
                _source.print("class ");
                _source.print(mapName);
                _source.print("_");
                _source.print(state.getClassName());
                _source.println(";");
            }

            // Forward declare the default state as well.
            _source.print(_indent);
            _source.print("class ");
            _source.print(mapName);
            _source.println("_Default;");
        }

        // Forward declare the state class and its
        // context as well.
        _source.print(_indent);
        _source.print("class ");
        _source.print(context);
        _source.println("State;");
        _source.print(_indent);
        _source.print("class ");
        _source.print(fsmClassName);
        _source.println(";");

        // Forward declare the application class.
        _source.print(_indent);
        _source.print("class ");
        _source.print(context);
        _source.println(";");

        // Do user-specified forward declarations now.
        for (String declaration: fsm.getDeclarations())
        {
            _source.print(_indent);
            _source.print(declaration);
            _source.println();
        }
        _source.println();

        // Declare user's base state class.
        _source.print(_indent);
        _source.print("class ");
        _source.print(context);
        _source.println("State :");
        _source.print(_indent);
        _source.println("    public statemap::State");
        _source.print(_indent);
        _source.println("{");
        _source.print(_indent);
        _source.println("public:");
        _source.println();

        // Constructor.
        _source.print(_indent);
        _source.print("    ");
        _source.print(context);
        _source.println("State(const char * const name, const int stateId)");
        _source.print(_indent);
        _source.println("    : statemap::State(name, stateId)");
        _source.print(_indent);
        _source.println("    {};");
        _source.println();

        // Add the default Entry() and Exit() definitions.
        _source.print(_indent);
        _source.print("    virtual void Entry(");
        _source.print(fsmClassName);
        _source.println("&) {};");
        _source.print(_indent);
        _source.print("    virtual void Exit(");
        _source.print(fsmClassName);
        _source.println("&) {};");
        _source.println();

        // Print out the default definitions for all the
        // transitions. First, get the transitions list.
        transList = fsm.getTransitions();

        // Output the global transition declarations.
        for (SmcTransition trans: transList)
        {
            // Don't output the default state here.
            if (trans.getName().equals("Default") == false)
            {
                _source.print(_indent);
                _source.print("    virtual void ");
                _source.print(trans.getName());
                _source.print("(");
                _source.print(fsmClassName);
                _source.print("& context");

                params = trans.getParameters();
                for (SmcParameter param: params)
                {
                    _source.print(", ");
                    param.accept(this);
                }

                _source.println(");");
            }
        }

        // Declare the global Default transition.
        _source.println();
        _source.print(_indent);
        _source.println("protected:");
        _source.println();
        _source.print(_indent);
        _source.print("    virtual void Default(");
        _source.print(fsmClassName);
        _source.println("& context);");

        // The base class has been defined.
        _source.print(_indent);
        _source.println("};");
        _source.println();

        // Generate the map classes. The maps will, in turn,
        // generate the state classes.
        for (SmcMap map: fsm.getMaps())
        {
            map.accept(this);
        }

        // Generate the FSM context class.
        // class FooContext :
        //     public statemap::FSMContext
        // {
        // public:
        //     FOOContext(FOO& owner)
        //     virtual void enterStartState()
        //
        _source.print(_indent);
        _source.print("class ");
        _source.print(fsmClassName);
        _source.println(" :");
        _source.print(_indent);
        _source.println("    public statemap::FSMContext");
        _source.print(_indent);
        _source.println("{");
        _source.print(_indent);
        _source.println("public:");
        _source.println();
        _source.print(_indent);
        _source.print("    explicit ");
        _source.print(fsmClassName);
        _source.print("(");
        _source.print(context);
        _source.println("& owner)");
        _source.print(_indent);
        _source.print("    : FSMContext(");
        _source.print(fsm.getStartState());
        _source.println("),");
        _source.print(_indent);
        _source.println("      _owner(&owner)");
        _source.print(_indent);
        _source.println("    {};");
        _source.println();
        _source.print(_indent);
        _source.print("    ");
        _source.print(fsmClassName);
        _source.print("(");
        _source.print(context);
        _source.println(
            "& owner, const statemap::State& state)");
        _source.print(_indent);
        _source.println("    : FSMContext(state),");
        _source.print(_indent);
        _source.println("      _owner(&owner)");
        _source.print(_indent);
        _source.println("    {};");
        _source.println();
        _source.print(_indent);
        _source.println("    virtual void enterStartState()");
        _source.print(_indent);
        _source.println("    {");
        _source.print(_indent);
        _source.println("        getState().Entry(*this);");
        _source.print(_indent);
        _source.println("    }");
        _source.println();
        _source.print(_indent);
        _source.print("    inline ");
        _source.print(context);
        _source.println("& getOwner()");
        _source.print(_indent);
        _source.println("    {");
        _source.print(_indent);
        _source.println("        return *_owner;");
        _source.print(_indent);
        _source.println("    };");
        _source.println();
        _source.print(_indent);
        _source.print("    inline ");
        _source.print(context);
        _source.println("State& getState()");
        _source.print(_indent);
        _source.println("    {");

        // v. 1.3.1: If -noex was specified, then don't throw
        // exceptions.
        if (_noExceptionFlag == false)
        {
            _source.print(_indent);
            _source.println("        if (_state == NULL)");
            _source.print(_indent);
            _source.println("        {");
            _source.print(_indent);
            _source.print("            throw ");
            _source.println(
                "statemap::StateUndefinedException();");
            _source.print(_indent);
            _source.println("        }");
        }
        else
        {
            _source.print(_indent);
            _source.println("        assert(_state != NULL);");
        }

        _source.println();
        _source.print(_indent);
        _source.print("        return ");
        _source.print(_castType);
        _source.print("<");
        _source.print(context);
        _source.println("State&>(*_state);");
        _source.print(_indent);
        _source.println("    };");

        // Generate a method for every transition in every map
        // *except* the default transition.
        for (SmcTransition trans: transList)
        {
            if (trans.getName().equals("Default") == false)
            {
                _source.println();
                _source.print(_indent);
                _source.print("    inline void ");
                _source.print(trans.getName());
                _source.print("(");

                params = trans.getParameters();
                for (pit = params.iterator(),
                       separator = "";
                     pit.hasNext() == true;
                     separator = ", ")
                {
                    _source.print(separator);
                    (pit.next()).accept(this);
                }
                _source.println(")");
                _source.print(_indent);
                _source.println("    {");

                // If -g was specified, then set the transition
                // name so it can be printed out.
                if (_debugLevel >= DEBUG_LEVEL_0)
                {
                    _source.print(_indent);
                    _source.print("        setTransition(\"");
                    _source.print(trans.getName());
                    _source.println("\");");
                }

                _source.print(_indent);
                _source.print("        getState().");
                _source.print(trans.getName());
                _source.print("(*this");
                for (SmcParameter param: params)
                {
                    _source.print(", ");
                    _source.print(param.getName());
                }
                _source.println(");");

                if (_debugLevel >= DEBUG_LEVEL_0)
                {
                    _source.print(_indent);
                    _source.println(
                        "        setTransition(NULL);");
                }

                _source.print(_indent);
                _source.println("    };");
            }
        }

        // v. 2.2.0: If we are supporting serialization, then
        // declare the valueOf static method.
        if (_serialFlag == true)
        {
            _source.println();
            _source.print(_indent);
            _source.print("    static ");
            _source.print(context);
            _source.println("State& valueOf(int stateId);");
        }

        // Member data.
        _source.println();
        _source.print(_indent);
        _source.println("private:");
        _source.println();
        _source.print(_indent);
        _source.print("    ");
        _source.print(context);
        _source.println("* _owner;");

        // v. 2.2.0: If we are supporting serialization, then
        // declare the min and max indices.
        if (_serialFlag == true)
        {
            _source.println();
            _source.print(_indent);
            _source.println("private:");
            _source.println();
            _source.print(_indent);
            _source.println("    const static int MIN_INDEX;");
            _source.print(_indent);
            _source.println("    const static int MAX_INDEX;");
            _source.print(_indent);
            _source.print("    static ");
            _source.print(context);
            _source.println("State* _States[];");
        }

        // Put the closing brace on the context class.
        _source.print(_indent);
        _source.println("};");

        // If necessary, place an end brace for the namespace.
        if (packageName != null && packageName.length() > 0)
        {
            int i;
            int j;

            for (i = (packageDepth - 1); i >= 0; --i)
            {
                // Output the proper indent.
                for (j = 0; j < i; ++j)
                {
                    _source.print("    ");
                }

                _source.println("}");
                _source.println();
            }
        }
        else
        {
            _source.println();
        }

        _source.println();
        _source.print("#endif // ");
        _source.print(targetfileCaps);
        _source.println("_H");

        _source.println();
        _source.println("//");
        _source.println("// Local variables:");
        _source.println("//  buffer-read-only: t");
        _source.println("// End:");
        _source.println("//");

        return;
    } // end of visit(SmcFSM)

    /**
     * Generates the map class declaration and then the state
     * classes:
     * <code>
     * class <i>map name</i>
     * {
     * public:
     *
     *     static <i>map name</i>_<i>state name</i> <i>state name</i>;
     * };
     * </code>
     * @param map emit C++ header code for this map.
     */
    // 
    //
    public void visit(SmcMap map)
    {
        String context = map.getFSM().getContext();
        String mapName = map.getName();
        String stateName;

        // Forward declare the map.
        _source.print(_indent);
        _source.print("class ");
        _source.println(mapName);
        _source.print(_indent);
        _source.println("{");
        _source.print(_indent);
        _source.println("public:");
        _source.println();

        // Iterate over the map's states and declare the static,
        // singleton state instances
        for (SmcState state: map.getStates())
        {
            stateName = state.getClassName();

            _source.print(_indent);
            _source.print("    static ");
            _source.print(mapName);
            _source.print("_");
            _source.print(stateName);
            _source.print(" ");
            _source.print(stateName);
            _source.println(";");
        }

        // The map class is now defined.
        _source.print(_indent);
        _source.println("};");
        _source.println();

        // Declare the map's default state class.
        //
        // class <map name>_Default :
        //     public <context>State
        // {
        // public:
        //
        //     <map name>_Default(const char * const name, const int stateId)
        //     : <context>State(name, stateId)
        //     {};
        //
        //     (user-defined Default state transitions.)
        // };
        _source.print(_indent);
        _source.print("class ");
        _source.print(mapName);
        _source.println("_Default :");
        _source.print(_indent);
        _source.print("    public ");
        _source.print(context);
        _source.println("State");
        _source.print(_indent);
        _source.println("{");
        _source.print(_indent);
        _source.println("public:");
        _source.println();

        // Default state's constructor.
        _source.print(_indent);
        _source.print("    ");
        _source.print(mapName);
        _source.println(
            "_Default(const char * const name, const int stateId)");
        _source.print(_indent);
        _source.print("    : ");
        _source.print(context);
        _source.println("State(name, stateId)");
        _source.print(_indent);
        _source.println("    {};");
        _source.println();

        // Declare the user-defined default transitions first.
        if (map.hasDefaultState() == true)
        {
            SmcState defaultState = map.getDefaultState();

            for (SmcTransition transition:
                     defaultState.getTransitions())
            {
                transition.accept(this);
            }
        }

        // The map's default state class is now defined.
        _source.print(_indent);
        _source.println("};");
        _source.println();

        // Now output the state class declarations.
        for (SmcState state: map.getStates())
        {
            state.accept(this);
        }

        return;
    } // end of visit(SmcMap)

    /**
     * Generates the state's class declaration:
     * <code>
     * class <i>map name</i>_<i>state name</i> :
     *     public <i>map name</i>_Default
     * {
     * public:
     *
     *     <i>map name</i>_<i>state name</i>(const char * const name, const int stateId)
     *     : <i>map name</i>_Default(name, stateId)
     *     {};
     *
     *     (declare the transition methods.)
     *     void <i>transition name</i>(<i>context</i> context, <i>args</i>);
     * };
     * </code>
     * @param state emits C++ header code for this state.
     */
    public void visit(SmcState state)
    {
        SmcMap map = state.getMap();
        String context = map.getFSM().getContext();
        String fsmClassName = map.getFSM().getFsmClassName();
        String mapName = map.getName();
        String stateName = state.getClassName();
        List<SmcAction> actions;

        _source.print(_indent);
        _source.print("class ");
        _source.print(mapName);
        _source.print('_');
        _source.print(stateName);
        _source.println(" :");
        _source.print(_indent);
        _source.print("    public ");
        _source.print(mapName);
        _source.println("_Default");
        _source.print(_indent);
        _source.println("{");
        _source.print(_indent);
        _source.print("public:");
        _source.println();
        _source.print(_indent);
        _source.print("    ");
        _source.print(mapName);
        _source.print('_');
        _source.print(stateName);
        _source.println("(const char * const name, const int stateId)");
        _source.print(_indent);
        _source.print("    : ");
        _source.print(mapName);
        _source.println("_Default(name, stateId)");
        _source.print(_indent);
        _source.println("    {};");
        _source.println();

        // Add the Entry() and Exit() methods if this state
        // defines them.
        actions = state.getEntryActions();
        if (actions != null && actions.size() > 0)
        {
            _source.print(_indent);
            _source.print("    virtual void Entry(");
            _source.print(fsmClassName);
            _source.println("&);");
        }

        actions = state.getExitActions();
        if (actions != null && actions.size() > 0)
        {
            _source.print(_indent);
            _source.print("    virtual void Exit(");
            _source.print(fsmClassName);
            _source.println("&);");
        }

        // Now generate the transition methods.
        for (SmcTransition transition: state.getTransitions())
        {
            transition.accept(this);
        }

        // End of the state class declaration.
        _source.print(_indent);
        _source.println("};");
        _source.println();

        return;
    } // end of visit(SmcState)

    /**
     * Generates the transition method declaration:
     * <code>
     * void <i>transition name</i>(<i>context</i>Context&amp; context, <i>args</i>);
     * </code>
     * @param transition emits C++ header code for this state
     * transition.
     */
    public void visit(SmcTransition transition)
    {
        SmcState state = transition.getState();
        String stateName = state.getClassName();
        String virtual = "";

        _source.print(_indent);
        _source.print("    virtual void ");
        _source.print(transition.getName());
        _source.print("(");
        _source.print(
            state.getMap().getFSM().getFsmClassName());
        _source.print("& context");

        // Add user-defined parameters.
        for (SmcParameter param: transition.getParameters())
        {
            _source.print(", ");
            param.accept(this);
        }

        // End of transition method declaration.
        _source.println(");");

        return;
    } // end of visit(SmcTransition)

    /**
     * Emits C++ header code for this transition parameter.
     * @param parameter emits C++ header code for this transition
     * parameter.
     */
    public void visit(SmcParameter parameter)
    {
        _source.print(parameter.getType());
        _source.print(" ");
        _source.print(parameter.getName());

        return;
    } // end of visit(SmcParameter)

    //
    // end of SmcVisitor Abstract Method Impelementation.
    //-----------------------------------------------------------

//---------------------------------------------------------------
// Member data
//
} // end of class SmcHeaderGenerator

//
// CHANGE LOG
// $Log: SmcHeaderGenerator.java,v $
// Revision 1.9  2014/09/13 06:25:32  fperrad
// refactor C++ generation
//
// Revision 1.8  2012/04/21 10:04:06  fperrad
// fix 3518773 : remove additional ';' with '%declare'
//
// Revision 1.7  2010/02/15 18:05:43  fperrad
// fix 2950619 : make distinction between source filename (*.sm) and target filename.
//
// Revision 1.6  2009/11/25 22:30:19  cwrapp
// Fixed problem between %fsmclass and sm file names.
//
// Revision 1.5  2009/11/24 20:42:39  cwrapp
// v. 6.0.1 update
//
// Revision 1.4  2009/09/12 21:44:49  kgreg99
// Implemented feature req. #2718941 - user defined generated class name.
// A new statement was added to the syntax: %fsmclass class_name
// It is optional. If not used, generated class is called as before "XxxContext" where Xxx is context class name as entered via %class statement.
// If used, generated class is called asrequested.
// Following language generators are touched:
// c, c++, java, c#, objc, lua, groovy, scala, tcl, VB
// This feature is not tested yet !
// Maybe it will be necessary to modify also the output file name.
//
// Revision 1.3  2009/09/05 15:39:20  cwrapp
// Checking in fixes for 1944542, 1983929, 2731415, 2803547 and feature 2797126.
//
// Revision 1.2  2009/03/27 09:41:47  cwrapp
// Added F. Perrad changes back in.
//
// Revision 1.1  2009/03/01 18:20:42  cwrapp
// Preliminary v. 6.0.0 commit.
//
//
