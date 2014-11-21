package com.bymarcin.openglasses.surface.widgets.core.luafunction;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;

import com.bymarcin.openglasses.lua.LuaFunction;
import com.bymarcin.openglasses.surface.Widget;
import com.bymarcin.openglasses.surface.widgets.core.attribute.IVertex;

public class GetVertexCount extends LuaFunction{

	@Override
	public Object[] call(Context context, Arguments arguments) {
		Widget widget = getSelf().getWidget(); 
		if(widget instanceof IVertex){
			return new Object[]{((IVertex) widget).getVertexCount()};
		}
		throw new RuntimeException("Component does not exists!");
	}

	@Override
	public String getName() {
		return "getVertexCount";
	}

}
