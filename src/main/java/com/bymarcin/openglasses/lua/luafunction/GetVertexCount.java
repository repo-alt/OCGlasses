package com.bymarcin.openglasses.lua.luafunction;

import ben_mkiv.rendertoolkit.common.widgets.core.attribute.ICustomShape;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;

import com.bymarcin.openglasses.lua.LuaFunction;
import ben_mkiv.rendertoolkit.common.widgets.Widget;

public class GetVertexCount extends LuaFunction{

	@Override
	public Object[] call(Context context, Arguments arguments) {
		super.call(context, arguments);
		Widget widget = getSelf().getWidget(); 
		if(widget instanceof ICustomShape){
			return new Object[]{((ICustomShape) widget).getVertexCount()};
		}
		throw new RuntimeException("Component does not exists!");
	}

	@Override
	public String getName() {
		return "getVertexCount";
	}

}
