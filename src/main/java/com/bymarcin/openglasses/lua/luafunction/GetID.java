package com.bymarcin.openglasses.lua.luafunction;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import com.bymarcin.openglasses.lua.LuaFunction;

public class GetID extends LuaFunction{

	@Override
	@Callback(direct = true)
	public Object[] call(Context context, Arguments arguments) {
		super.call(context, arguments);
		return new Object[]{getSelf().getWidgetRef()};
	}

	@Override
	public String getName() {
		return "getID";
	}

}
