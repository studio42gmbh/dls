// <editor-fold desc="The MIT License" defaultstate="collapsed">
/* 
 * The MIT License
 * 
 * Copyright 2022 Studio 42 GmbH ( https://www.s42m.de ).
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
//</editor-fold>
module de.sft.dls
{
	requires java.desktop;
	requires java.naming;
	requires java.sql;
	requires javaee.web.api;
	requires de.sft.dl;
	requires de.sft.dlt;
	requires de.sft.log;
	requires de.sft.base;
	requires activation;
	requires java.mail;
	requires org.json;

	exports de.s42.dl.services; 
	exports de.s42.dl.services.content; 
	exports de.s42.dl.services.content.dlt; 
	exports de.s42.dl.services.database; 
	exports de.s42.dl.services.database.query; 
	exports de.s42.dl.services.database.postgres; 
	exports de.s42.dl.services.email; 
	exports de.s42.dl.services.l10n; 
	exports de.s42.dl.services.permission; 
	exports de.s42.dl.services.remote; 
	exports de.s42.dl.services.remote.parameters; 
	exports de.s42.dl.srv;
}
