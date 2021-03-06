/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.edit.EditController;
import org.eclipse.nebula.widgets.nattable.edit.event.InlineCellEditEvent;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.widgets.Composite;

/**
 * Command handler for handling {@link EditSelectionCommand}s.
 * Will first check if all selected cells are editable and if they have the same
 * editor configured. Will call the {@link EditController} for activation of the 
 * edit mode if these checks succeed.
 */
public class EditSelectionCommandHandler extends AbstractLayerCommandHandler<EditSelectionCommand> {

	private SelectionLayer selectionLayer;
	
	public EditSelectionCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}
	
	public Class<EditSelectionCommand> getCommandClass() {
		return EditSelectionCommand.class;
	}
	
	@Override
    public boolean doCommand(EditSelectionCommand command) {
		Composite parent = command.getParent();
		IConfigRegistry configRegistry = command.getConfigRegistry();
		Character initialValue = command.getCharacter();
		
		if (EditUtils.allCellsEditable(this.selectionLayer, configRegistry)
				&& EditUtils.isEditorSame(this.selectionLayer, configRegistry)
				&& EditUtils.isConverterSame(this.selectionLayer, configRegistry)) {
			//check how many cells are selected
			PositionCoordinate[] selectedCellPositions = this.selectionLayer.getSelectedCellPositions();
			if (selectedCellPositions.length == 1) {
				//editing is triggered by key for a single cell
				//we need to fire the InlineCellEditEvent here because we don't know the correct bounds
				//of the cell to edit inline corresponding to the NatTable. On firing the event, a
				//translation process is triggered, converting the information to the correct values
				//needed for inline editing
				PositionCoordinate selectedCell = selectedCellPositions[0];
				ILayerCell cell = this.selectionLayer.getCellByPosition(selectedCell.columnPosition, selectedCell.rowPosition);
				this.selectionLayer.fireLayerEvent(
						new InlineCellEditEvent(
								this.selectionLayer, 
								new PositionCoordinate(this.selectionLayer, selectedCell.columnPosition, selectedCell.rowPosition), 
								parent, 
								configRegistry, 
								(initialValue != null ? initialValue : cell.getDataValue())));
			}
			else if (selectedCellPositions.length > 1) {
				
				//collect the selected ILayerCells for further processing
				List<ILayerCell> selectedCells = new ArrayList<ILayerCell>();
				for (PositionCoordinate coord : selectedCellPositions) {
					selectedCells.add(this.selectionLayer.getCellByPosition(coord.columnPosition, coord.rowPosition));
				}
				
				//determine the initial value
				Object initialEditValue = initialValue;
				if (initialValue == null && EditUtils.isValueSame(this.selectionLayer)) {
					initialEditValue = this.selectionLayer.getDataValueByPosition(
							selectedCellPositions[0].columnPosition, selectedCellPositions[0].rowPosition);
				}
				
				EditController.editCells(selectedCells, parent, initialEditValue, configRegistry);
			}
		}

		//as commands by default are intended to be consumed by the handler, always
		//return true, whether the activation of the edit mode was successfull or not
		return true;
	}

}
