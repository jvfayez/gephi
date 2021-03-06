/*
Copyright 2011 Elijah Meeks
Authors : Elijah Meeks <emeeks@stanford.edu>
Website : http://dhs.stanford.edu


DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Elijah Meeks. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

 * 
 * 
 * 
 */
package org.elijahmeeks.tubemap;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author elijahmeeks
 */
    @ServiceProvider(service=LayoutBuilder.class)
public class TubeMap implements LayoutBuilder {

    private TubeMapLayoutUI ui = new TubeMapLayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(TubeMap.class, "TubeMap.name");
    }

    @Override
    public Layout buildLayout() {
        return new TubeMapLayout(this, 50);
    }

    public LayoutUI getUI() {
        return ui;
    }

    private static class TubeMapLayoutUI implements LayoutUI {

        public String getDescription() {
            return NbBundle.getMessage(TubeMap.class, "TubeMap.description");
        }

        public Icon getIcon() {
            return null;
        }

        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        public int getQualityRank() {
            return -1;
        }

        public int getSpeedRank() {
            return -1;
        }
    }
}
